package de.entwicklertraining.deepseek4j.chat.completion;

import de.entwicklertraining.deepseek4j.DeepSeekResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a response from the DeepSeek Chat Completion API.
 * Provides methods to access various parts of the response data.
 */
public final class DeepSeekCompletionResponse extends DeepSeekResponse<DeepSeekCompletionRequest> {

    /**
     * Constructs a DeepSeekCompletionResponse.
     *
     * @param json    The raw JSON response object.
     * @param request The original request that led to this response.
     */
    public DeepSeekCompletionResponse(JSONObject json, DeepSeekCompletionRequest request) {
        super(json, request);
    }

    // Top-Level Fields Getters [cite: 21]

    /**
     * Gets the unique identifier for the chat completion.
     * @return The ID string, or null if not present. [cite: 30]
     */
    public String getId() {
        return getJson().optString("id", null); // [cite: 30]
    }

    /**
     * Gets the object type, which is always "chat.completion".
     * @return The object type string, or null if not present.
     */
    public String getObject() {
        return getJson().optString("object", null);
    }

    /**
     * Gets the Unix timestamp (in seconds) of when the chat completion was created.
     * @return The creation timestamp, or 0 if not present.
     */
    public long getCreated() {
        return getJson().optLong("created", 0L);
    }

    /**
     * Gets the model used for the chat completion.
     * @return The model ID string, or null if not present.
     */
    public String getModel() {
        return getJson().optString("model", null);
    }

    /**
     * Gets the system fingerprint representing the backend configuration used for the request.
     * @return The system fingerprint string, or null if not present.
     */
    public String getSystemFingerprint() {
        return getJson().optString("system_fingerprint", null);
    }

    /**
     * Gets the list of chat completion choices.
     * @return A list of Choice objects, or an empty list if not present or invalid. [cite: 31, 32]
     */
    public List<Choice> getChoices() {
        JSONArray choicesArray = getJson().optJSONArray("choices"); // [cite: 31]
        if (choicesArray == null) {
            return Collections.emptyList(); // [cite: 32]
        }
        List<Choice> choices = new ArrayList<>(); // [cite: 33]
        for (int i = 0; i < choicesArray.length(); i++) { // [cite: 34]
            JSONObject choiceJson = choicesArray.optJSONObject(i); // [cite: 34]
            if (choiceJson != null) {
                choices.add(new Choice(choiceJson)); // [cite: 35]
            }
        }
        return choices; // [cite: 36]
    }

    /**
     * Gets the usage statistics for the completion request.
     * @return A Usage object, or null if the "usage" field is not present or invalid. [cite: 27]
     */
    public Usage getUsage() {
        JSONObject usageJson = getJson().optJSONObject("usage");
        return (usageJson != null) ? new Usage(usageJson) : null;
    }

    /**
     * Convenience method to get the message content from the first choice.
     * @return The content string, or null if no choices or message content exists.
     */
    public String assistantMessage() {
        return getChoices().stream()
                .findFirst()
                .map(Choice::getMessage)
                .map(Message::getContent)
                .orElse(null);
    }

    /**
     * Convenience method to get the finish reason from the first choice.
     * @return The finish reason string (e.g., "stop", "tool_calls"), or null if no choices exist.
     */
    public String finishReason() {
        return getChoices().stream()
                .findFirst()
                .map(Choice::getFinishReason)
                .orElse(null);
    }

    // --- Helper Classes [cite: 22] ---

    /**
     * Represents a single choice in the chat completion response. [cite: 23]
     */
    public static class Choice {
        private final JSONObject json;

        Choice(JSONObject json) { // [cite: 35]
            this.json = (json != null) ? json : new JSONObject();
        }

        public int getIndex() {
            return json.optInt("index", 0);
        }

        public String getFinishReason() {
            return json.optString("finish_reason", null);
        }

        public Message getMessage() {
            JSONObject messageJson = json.optJSONObject("message");
            return (messageJson != null) ? new Message(messageJson) : null;
        }

        public Logprobs getLogprobs() {
            JSONObject logprobsJson = json.optJSONObject("logprobs");
            return (logprobsJson != null) ? new Logprobs(logprobsJson) : null;
        }
    }

    /**
     * Represents a message within a choice, including content and potential tool calls. [cite: 23]
     */
    public static class Message {
        private final JSONObject json;

        Message(JSONObject json) {
            this.json = (json != null) ? json : new JSONObject();
        }

        public String getRole() {
            return json.optString("role", null);
        }

        /**
         * Gets the primary content of the message.
         * Note: For deepseek-reasoner, this might be combined content if reasoning_content is present.
         */
        public String getContent() {
            return json.optString("content", null);
        }

        /**
         * Gets the reasoning content (specific to deepseek-reasoner).
         * @return Reasoning content string, or null if not present.
         */
        public String getReasoningContent() {
            return json.optString("reasoning_content", null); // [cite: 23]
        }


        public List<ToolCall> getToolCalls() {
            JSONArray toolCallsArray = json.optJSONArray("tool_calls");
            if (toolCallsArray == null) {
                return Collections.emptyList();
            }
            List<ToolCall> toolCalls = new ArrayList<>();
            for (int i = 0; i < toolCallsArray.length(); i++) {
                JSONObject toolCallJson = toolCallsArray.optJSONObject(i);
                if (toolCallJson != null) {
                    toolCalls.add(new ToolCall(toolCallJson));
                }
            }
            return toolCalls;
        }
    }

    /**
     * Represents a tool call requested by the model. [cite: 24]
     */
    public static class ToolCall {
        private final JSONObject json;

        ToolCall(JSONObject json) {
            this.json = (json != null) ? json : new JSONObject();
        }

        public String getId() {
            return json.optString("id", null);
        }

        /** Always "function" for now. */
        public String getType() {
            return json.optString("type", null);
        }

        public FunctionCall getFunction() {
            JSONObject functionJson = json.optJSONObject("function");
            return (functionJson != null) ? new FunctionCall(functionJson) : null;
        }
    }

    /**
     * Represents the function details within a tool call. [cite: 24]
     */
    public static class FunctionCall {
        private final JSONObject json;

        FunctionCall(JSONObject json) {
            this.json = (json != null) ? json : new JSONObject();
        }

        public String getName() {
            return json.optString("name", null);
        }

        /** The arguments as a JSON string. */
        public String getArguments() {
            return json.optString("arguments", null);
        }

        /**
         * Convenience method to parse arguments into a JSONObject.
         * @return Arguments as JSONObject, or an empty JSONObject if parsing fails or args are null/empty.
         */
        public JSONObject getArgumentsAsJson() {
            String args = getArguments();
            if (args == null || args.isEmpty()) {
                return new JSONObject();
            }
            try {
                return new JSONObject(args);
            } catch (Exception e) {
                // Log parsing error or handle appropriately
                System.err.println("Warning: Could not parse function arguments as JSON: " + args);
                return new JSONObject(); // Return empty object on failure
            }
        }
    }

    /**
     * Represents log probability information for the completion. [cite: 25]
     */
    public static class Logprobs {
        private final JSONObject json;

        Logprobs(JSONObject json) {
            this.json = (json != null) ? json : new JSONObject();
        }

        public List<TokenLogprob> getContent() {
            JSONArray contentArray = json.optJSONArray("content");
            if (contentArray == null) {
                return Collections.emptyList();
            }
            List<TokenLogprob> content = new ArrayList<>();
            for (int i = 0; i < contentArray.length(); i++) {
                JSONObject tokenLogprobJson = contentArray.optJSONObject(i);
                if (tokenLogprobJson != null) {
                    content.add(new TokenLogprob(tokenLogprobJson));
                }
            }
            return content;
        }
    }

    /**
     * Represents log probability information for a single token. [cite: 25]
     */
    public static class TokenLogprob {
        private final JSONObject json;

        TokenLogprob(JSONObject json) {
            this.json = (json != null) ? json : new JSONObject();
        }

        public String getToken() {
            return json.optString("token", null);
        }

        public double getLogprob() {
            // Use optDouble which returns NaN if not found or not a number
            return json.optDouble("logprob", Double.NaN);
        }

        /** List of integers representing the UTF-8 bytes sequence. */
        public List<Integer> getBytes() {
            JSONArray bytesArray = json.optJSONArray("bytes");
            if (bytesArray == null) {
                // Handle null array case: return empty list or null as appropriate
                return Collections.emptyList(); // Or return null if that's preferred
            }
            List<Integer> bytesList = new ArrayList<>();
            for (int i = 0; i < bytesArray.length(); i++) {
                // Use optInt or getInt depending on whether you expect non-integers
                // optInt provides a default if the value is missing or not an int
                bytesList.add(bytesArray.optInt(i));
            }
            return bytesList;
        }


        public List<TopTokenLogprob> getTopLogprobs() {
            JSONArray topLogprobsArray = json.optJSONArray("top_logprobs");
            if (topLogprobsArray == null) {
                return Collections.emptyList();
            }
            List<TopTokenLogprob> topLogprobs = new ArrayList<>();
            for (int i = 0; i < topLogprobsArray.length(); i++) {
                JSONObject topTokenLogprobJson = topLogprobsArray.optJSONObject(i);
                if (topTokenLogprobJson != null) {
                    topLogprobs.add(new TopTokenLogprob(topTokenLogprobJson));
                }
            }
            return topLogprobs;
        }
    }

    /**
     * Represents log probability information for one of the top alternative tokens. [cite: 26]
     */
    public static class TopTokenLogprob {
        private final JSONObject json;

        TopTokenLogprob(JSONObject json) {
            this.json = (json != null) ? json : new JSONObject();
        }

        public String getToken() {
            return json.optString("token", null);
        }

        public double getLogprob() {
            return json.optDouble("logprob", Double.NaN);
        }

        /** List of integers representing the UTF-8 bytes sequence. */
        public List<Integer> getBytes() {
            JSONArray bytesArray = json.optJSONArray("bytes");
            if (bytesArray == null) {
                return Collections.emptyList();
            }
            // Efficient conversion if primitive array is acceptable upstream
            // return IntStream.range(0, bytesArray.length()).map(bytesArray::optInt).boxed().collect(Collectors.toList());

            // More conventional loop:
            List<Integer> bytesList = new ArrayList<>();
            for (int i = 0; i < bytesArray.length(); i++) {
                bytesList.add(bytesArray.optInt(i));
            }
            return bytesList;
        }
    }


    /**
     * Represents usage statistics for the API call. [cite: 27]
     */
    public static class Usage {
        private final JSONObject json;

        Usage(JSONObject json) {
            this.json = (json != null) ? json : new JSONObject();
        }

        public int getCompletionTokens() {
            return json.optInt("completion_tokens", 0);
        }

        public int getPromptTokens() {
            return json.optInt("prompt_tokens", 0);
        }

        /** Gets the number of prompt tokens served from cache. */
        public int getPromptCacheHitTokens() {
            return json.optInt("prompt_cache_hit_tokens", 0);
        }

        /** Gets the number of prompt tokens not served from cache. */
        public int getPromptCacheMissTokens() {
            return json.optInt("prompt_cache_miss_tokens", 0);
        }

        public int getTotalTokens() {
            return json.optInt("total_tokens", 0);
        }

        /**
         * Gets detailed token counts, specifically for reasoning tokens (optional).
         * @return A CompletionTokensDetails object, or null if not present. [cite: 27]
         */
        public CompletionTokensDetails getCompletionTokensDetails() {
            JSONObject detailsJson = json.optJSONObject("completion_tokens_details");
            return (detailsJson != null) ? new CompletionTokensDetails(detailsJson) : null;
        }
    }

    /**
     * Represents detailed token counts within the completion usage. Currently only includes reasoning_tokens. [cite: 27]
     */
    public static class CompletionTokensDetails {
        private final JSONObject json;

        CompletionTokensDetails(JSONObject json) {
            this.json = (json != null) ? json : new JSONObject();
        }

        /**
         * Gets the number of tokens used for reasoning steps (specific to deepseek-reasoner).
         * @return The count of reasoning tokens, or 0 if not present. [cite: 27]
         */
        public int getReasoningTokens() {
            return json.optInt("reasoning_tokens", 0);
        }
    }
}