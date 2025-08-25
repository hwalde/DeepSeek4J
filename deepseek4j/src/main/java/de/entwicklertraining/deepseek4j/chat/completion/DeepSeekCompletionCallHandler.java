package de.entwicklertraining.deepseek4j.chat.completion;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.deepseek4j.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger; // Import Logger [cite: 38]
import org.slf4j.LoggerFactory; // Import LoggerFactory [cite: 38]


import java.util.*;

/**
 * DeepSeekCompletionCallHandler orchestrates sending a DeepSeekCompletionRequest,
 * handling any tool calls (function calls), and continuing the conversation until
 * a final answer is reached or the max turns is exceeded.
 *
 * Supports:
 * - Parallel tool calls if the server chooses (though the doc says we must handle them).
 * - Structured outputs (response_format) if the user set that in the request.
 * - "finish_reason" like "stop", "length", "content_filter", "tool_calls", "insufficient_system_resource"
 */
public final class DeepSeekCompletionCallHandler {

    private final DeepSeekClient client;

    private static final int MAX_TURNS = 4;
    // Logger hinzufügen [cite: 38]
    private static final Logger logger = LoggerFactory.getLogger(DeepSeekCompletionCallHandler.class);

    public DeepSeekCompletionCallHandler(DeepSeekClient client) {
        this.client = client;
    }

    /**
     * Main method: sends the conversation to DeepSeek, handles potential tool calls, and repeats
     * until a final response is reached or an error occurs.
     */
    public DeepSeekCompletionResponse handleRequest(DeepSeekCompletionRequest initialRequest, boolean useExponentialBackoff) {
        // Streaming-Unterstützung prüfen [cite: 57, 58]
        if (Boolean.TRUE.equals(initialRequest.stream())) {
            throw new UnsupportedOperationException("Streaming responses are not currently supported by this handler.");
        }

        // Modell-spezifische Validierung [cite: 39]
        validateRequestForModel(initialRequest);

        List<JSONObject> messages = new ArrayList<>(initialRequest.messages());
        Map<String, DeepSeekToolDefinition> toolMap = new HashMap<>();
        if (initialRequest.tools() != null) { // Null check added for safety
            for (var t : initialRequest.tools()) {
                toolMap.put(t.name(), t);
            }
        }

        DeepSeekCompletionRequest currentRequest = initialRequest;
        int turnCount = 0;

        while (true) {
            turnCount++;
            if (turnCount > MAX_TURNS) {
                throw new ApiClient.ApiClientException("Exceeded maximum of " + MAX_TURNS + " DeepSeek call iterations without final stop.");
            }

            // Send the request
            DeepSeekCompletionResponse response;
            if(useExponentialBackoff) {
                // Assuming DeepSeekClient handles its own exponential backoff logic or this method is deprecated/internal
                response = client.sendRequest(currentRequest);
            } else {
                response = client.sendRequestWithExponentialBackoff(currentRequest);
            }

            // Check if there's an "error" field in the JSON (rare)
            if (response.getJson().has("error")) {
                throw new ApiClient.ApiResponseUnusableException(
                        "DeepSeek API returned an error: " + response.getJson().toString()
                );
            }

            // Extract the first choice and message
            JSONObject firstChoice = response.getJson().optJSONArray("choices").optJSONObject(0);
            if (firstChoice == null) {
                throw new ApiClient.ApiResponseUnusableException(
                        "DeepSeek API response missing 'choices' array or first choice object. Response: " + response.getJson().toString()
                );
            }
            JSONObject assistantMessage = firstChoice.optJSONObject("message");
            if (assistantMessage == null) {
                throw new ApiClient.ApiResponseUnusableException(
                        "DeepSeek API response missing 'message' object in first choice. Response: " + response.getJson().toString()
                );
            }

            String finishReason = firstChoice.optString("finish_reason", null);


            // Add the assistant message to the conversation
            // NEU: Entferne reasoning_content für deepseek-reasoner, bevor es zur Historie hinzugefügt wird [cite: 54, 55]
            JSONObject messageToAdd = new JSONObject(assistantMessage.toString()); // Kopie erstellen
            if ("deepseek-reasoner".equals(currentRequest.model())) {
                messageToAdd.remove("reasoning_content");
            }
            messages.add(messageToAdd); // [cite: 56]


            // Extract tool_calls if any
            JSONArray toolCallsArray = assistantMessage.optJSONArray("tool_calls");


            if (toolCallsArray == null || toolCallsArray.isEmpty()) {
                // No further tool calls -> final
                if ("tool_calls".equals(finishReason)) {
                    // This case might indicate an API inconsistency, but we handle it gracefully.
                    logger.warn("DeepSeek finish_reason is 'tool_calls' but no 'tool_calls' array found in the message. Returning current response. Response: {}", response.getJson());
                }
                return response;
            } else if (!"tool_calls".equals(finishReason)) {
                // Log a warning if tools are present but finish_reason isn't 'tool_calls'
                logger.warn("DeepSeek response contains 'tool_calls' but finish_reason is '{}'. Processing tool calls anyway. Response: {}", finishReason, response.getJson());
            }


            // Otherwise, process each tool call
            for (int i = 0; i < toolCallsArray.length(); i++) {
                JSONObject toolCallObj = toolCallsArray.optJSONObject(i);
                if (toolCallObj == null) continue; // Skip invalid entries

                String toolCallId = toolCallObj.optString("id", null);
                JSONObject functionObj = toolCallObj.optJSONObject("function");

                if (toolCallId == null || functionObj == null) {
                    logger.error("Invalid tool_call entry missing 'id' or 'function': {}", toolCallObj);
                    // Decide whether to throw or continue; continuing might be more robust
                    continue;
                    // throw new ApiClient.ApiResponseUnusableException("Invalid 'tool_call' object missing 'id' or 'function'. " + toolCallObj);
                }

                String toolName = functionObj.optString("name", null);

                if (toolName == null || toolName.isBlank()) {
                    throw new ApiClient.ApiResponseUnusableException(
                            "Missing 'name' in tool call function object. " + toolCallObj
                    );
                }
                if (!toolMap.containsKey(toolName)) {
                    throw new ApiClient.ApiResponseUnusableException(
                            "Unknown tool call name referenced by model: " + toolName
                    );
                }

                // Parse arguments
                JSONObject args;
                String argsString = functionObj.optString("arguments", null);
                if (argsString == null) {
                    // Some functions might not require arguments
                    args = new JSONObject();
                    logger.warn("Missing 'arguments' string for tool call '{}'. Assuming empty arguments.", toolName);
                } else {
                    try {
                        args = new JSONObject(argsString);
                    } catch (Exception e) {
                        throw new ApiClient.ApiResponseUnusableException(
                                "Failed to parse arguments JSON string for tool call '" + toolName + "'. " +
                                        "Arguments: '" + argsString + "' Error: " + e.getMessage()
                        );
                    }
                }

                // Invoke the tool callback
                DeepSeekToolDefinition toolDef = toolMap.get(toolName);
                DeepSeekToolResult toolResult;
                try {
                    toolResult = toolDef.callback().handle(new DeepSeekToolCallContext(args));
                    if(toolResult == null || toolResult.content() == null) {
                        throw new IllegalStateException("Tool callback for '" + toolName + "' returned null result or null content.");
                    }
                } catch (Exception e) {
                    // Catch exceptions from the tool implementation itself
                    logger.error("Exception occurred during execution of tool '{}': {}", toolName, e.getMessage(), e);
                    throw new ApiClient.ApiClientException("Error executing tool '" + toolName + "': " + e.getMessage(), e);
                }


                // Append a new "tool" role message
                messages.add(new JSONObject()
                        .put("role", "tool")
                        .put("tool_call_id", toolCallId) // Use the parsed toolCallId
                        .put("content", toolResult.content()));
            }

            // Build a new request with updated messages
            currentRequest = buildNextRequest(initialRequest, messages);
        }
    }

    // Implementierung der Modell-spezifischen Validierung [cite: 39]
    private void validateRequestForModel(DeepSeekCompletionRequest request) {
        String model = request.model();
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model ID cannot be null or empty.");
        }

        if ("deepseek-reasoner".equals(model)) {
            // Warnungen für ignorierte Parameter [cite: 40, 42, 44, 46]
            // Note: Checking against null might not be enough if defaults are set.
            // A more robust check would compare against default values if known.
            if (request.temperature() != null /* && !request.temperature().equals(DEFAULT_TEMP) */) { // [cite: 40, 41]
                logger.warn("Parameter 'temperature' is ignored by model 'deepseek-reasoner'.");
            }
            if (request.topP() != null /* && !request.topP().equals(DEFAULT_TOP_P) */) { // [cite: 42, 43]
                logger.warn("Parameter 'top_p' is ignored by model 'deepseek-reasoner'.");
            }
            if (request.presencePenalty() != null /* && !request.presencePenalty().equals(DEFAULT_PRESENCE) */) { // [cite: 44, 45]
                logger.warn("Parameter 'presence_penalty' is ignored by model 'deepseek-reasoner'.");
            }
            if (request.frequencyPenalty() != null /* && !request.frequencyPenalty().equals(DEFAULT_FREQUENCY) */) { // [cite: 46, 47]
                logger.warn("Parameter 'frequency_penalty' is ignored by model 'deepseek-reasoner'.");
            }

            // Exceptions für nicht unterstützte Parameter/Features [cite: 48, 49, 50, 51, 52, 53]
            if (request.tools() != null && !request.tools().isEmpty()) { // [cite: 48]
                throw new IllegalArgumentException("Parameter 'tools' (function calling) is not supported by model 'deepseek-reasoner'.");
            }
            // Check tool_choice: 'auto' and 'none' are technically allowed but meaningless without tools.
            // The API docs state 'required' or specific function calls are disallowed.
            Object toolChoice = request.toolChoice();
            if (toolChoice != null) {
                if (toolChoice instanceof String) {
                    String tcStr = (String) toolChoice;
                    // Allow "auto" or "none" as per docs, even if they don't do anything without tools.
                    if (!"auto".equals(tcStr) && !"none".equals(tcStr)) { // [cite: 49]
                        throw new IllegalArgumentException("Parameter 'tool_choice' value '" + tcStr + "' is not supported by model 'deepseek-reasoner'. Only 'auto' or 'none' are implicitly allowed."); // [cite: 50]
                    }
                } else {
                    // If it's an object (specific function call), it's disallowed.
                    throw new IllegalArgumentException("Parameter 'tool_choice' (specifying a function) is not supported by model 'deepseek-reasoner'."); // [cite: 50]
                }
            }

            if (request.responseFormat() != null && "json_object".equals(request.responseFormat().type())) { // [cite: 51]
                throw new IllegalArgumentException("Response format 'json_object' is not supported by model 'deepseek-reasoner'.");
            }
            if (Boolean.TRUE.equals(request.logprobs())) { // [cite: 52]
                throw new IllegalArgumentException("Parameter 'logprobs' is not supported by model 'deepseek-reasoner'.");
            }
            if (request.topLogprobs() != null) { // [cite: 53]
                throw new IllegalArgumentException("Parameter 'top_logprobs' is not supported by model 'deepseek-reasoner'.");
            }
            // Beta feature checks (if relevant and implemented in request)
            // e.g., if (request.prefix() != null) { ... }
        }
        // Add checks for deepseek-chat if needed, though fewer restrictions apply.
        // else if ("deepseek-chat".equals(model)) { ... }
    }

    // parallelToolCalls Parameter entfernt [cite: 59]
    private DeepSeekCompletionRequest buildNextRequest(DeepSeekCompletionRequest initialReq, List<JSONObject> messages) {
        var builder = DeepSeekCompletionRequest.builder(client)
                .model(initialReq.model())
                .maxExecutionTimeInSeconds(initialReq.getMaxExecutionTimeInSeconds())
                .setCancelSupplier(initialReq.getIsCanceledSupplier())
                .addAllMessages(messages)
                // tools might be needed for subsequent calls if tool_choice wasn't 'none'
                .addAllTools(initialReq.tools())
                .responseFormat(initialReq.responseFormat())
                // .parallelToolCalls(initialReq.parallelToolCalls()) // Entfernt [cite: 59]
                .frequencyPenalty(initialReq.frequencyPenalty())
                .presencePenalty(initialReq.presencePenalty())
                .maxTokens(initialReq.maxTokens())
                .temperature(initialReq.temperature())
                .topP(initialReq.topP())
                .stop(initialReq.stop())
                .stream(initialReq.stream()) // Should be false based on earlier check, but pass it along
                .logprobs(initialReq.logprobs())
                .topLogprobs(initialReq.topLogprobs())
                // Pass tool_choice along for the next turn, model might decide differently
                .toolChoice(initialReq.toolChoice())
                // Pass streamOptions along as well
                .streamOptions(initialReq.streamOptions());


        if (initialReq.hasCaptureOnSuccess()) {
            builder.captureOnSuccess(initialReq.getCaptureOnSuccess());
        }
        if (initialReq.hasCaptureOnError()) {
            builder.captureOnError(initialReq.getCaptureOnError());
        }

        return builder.build();
    }
}