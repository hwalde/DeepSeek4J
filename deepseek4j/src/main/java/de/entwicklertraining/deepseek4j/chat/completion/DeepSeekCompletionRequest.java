package de.entwicklertraining.deepseek4j.chat.completion;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.deepseek4j.DeepSeekClient;
import de.entwicklertraining.deepseek4j.DeepSeekRequest;
import de.entwicklertraining.deepseek4j.DeepSeekResponseFormat;
import de.entwicklertraining.deepseek4j.DeepSeekToolDefinition;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a request to the DeepSeek Chat Completion API.
 *
 * Supports all documented parameters:
 * - messages (required)
 * - model (required)
 * - frequency_penalty
 * - max_tokens
 * - presence_penalty
 * - response_format
 * - stop
 * - stream
 * - stream_options
 * - temperature
 * - top_p
 * - tools
 * - tool_choice
 * - logprobs
 * - top_logprobs
 */
public final class DeepSeekCompletionRequest extends DeepSeekRequest<DeepSeekCompletionResponse> {

    private final String model;
    private final List<JSONObject> messages;
    private final Double frequencyPenalty;
    private final Integer maxTokens;
    private final Double presencePenalty;
    private final DeepSeekResponseFormat responseFormat;
    private final Object stop; // can be string or array of strings or null
    private final Boolean stream;

    private final DeepSeekStreamOptions streamOptions; // or null
    private final Double temperature;
    private final Double topP;
    private final List<DeepSeekToolDefinition> tools;
    private final Object toolChoice; // can be string or an object describing forced function
    // Removed parallelToolCalls field [cite: 17]
    private final Boolean logprobs; // optional
    private final Integer topLogprobs; // optional

    DeepSeekCompletionRequest(
            Builder builder,
            String model,
            List<JSONObject> messages,
            Double frequencyPenalty,
            Integer maxTokens,
            Double presencePenalty,
            DeepSeekResponseFormat responseFormat,
            Object stop,
            Boolean stream,
            // Changed type from JSONObject to DeepSeekStreamOptions [cite: 8]
            DeepSeekStreamOptions streamOptions,
            Double temperature,
            Double topP,
            List<DeepSeekToolDefinition> tools,
            Object toolChoice,
            // Removed parallelToolCalls parameter [cite: 17]
            Boolean logprobs,
            Integer topLogprobs
    ) {
        super(builder);
        this.model = model;
        this.messages = messages;
        this.frequencyPenalty = frequencyPenalty;
        this.maxTokens = maxTokens;
        this.presencePenalty = presencePenalty;
        this.responseFormat = responseFormat;
        this.stop = stop;
        this.stream = stream;
        this.streamOptions = streamOptions;
        this.temperature = temperature;
        this.topP = topP;
        this.tools = tools;
        this.toolChoice = toolChoice;
        // Removed parallelToolCalls assignment [cite: 17]
        this.logprobs = logprobs;
        this.topLogprobs = topLogprobs;
    }

    public String model() {
        return model;
    }

    public List<JSONObject> messages() {
        return messages;
    }

    public Double frequencyPenalty() {
        return frequencyPenalty;
    }

    public Integer maxTokens() {
        return maxTokens;
    }

    public Double presencePenalty() {
        return presencePenalty;
    }

    public DeepSeekResponseFormat responseFormat() {
        return responseFormat;
    }

    public Object stop() {
        return stop;
    }

    public Boolean stream() {
        return stream;
    }

    // Changed return type from JSONObject to DeepSeekStreamOptions [cite: 8]
    public DeepSeekStreamOptions streamOptions() {
        return streamOptions;
    }

    public Double temperature() {
        return temperature;
    }

    public Double topP() {
        return topP;
    }

    public List<DeepSeekToolDefinition> tools() {
        return tools;
    }

    public Object toolChoice() {
        return toolChoice;
    }

    // Removed parallelToolCalls() getter method [cite: 17]

    public Boolean logprobs() {
        return logprobs;
    }

    public Integer topLogprobs() {
        return topLogprobs;
    }

    @Override
    public String getRelativeUrl() {
        return "/chat/completions";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    @Override
    public String getBody() {
        return toJson().toString();
    }

    /**
     * Constructs the JSON body with all relevant fields.
     */
    public JSONObject toJson() {
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("messages", new JSONArray(messages));

        if (frequencyPenalty != null) {
            body.put("frequency_penalty", frequencyPenalty);
        }
        if (maxTokens != null) {
            body.put("max_tokens", maxTokens);
        }
        if (presencePenalty != null) {
            body.put("presence_penalty", presencePenalty);
        }
        if (responseFormat != null) {
            body.put("response_format", responseFormat.toJson());
        }
        if (stop != null) {
            // can be a single string or an array of strings
            body.put("stop", stop);
        }
        if (stream != null) {
            body.put("stream", stream);
        }
        // Updated to use toJson() method of DeepSeekStreamOptions [cite: 9]
        if (streamOptions != null) {
            JSONObject streamOptsJson = streamOptions.toJson();
            // Only add if the object is not empty
            if (streamOptsJson.length() > 0) {
                body.put("stream_options", streamOptsJson);
            }
        }
        if (temperature != null) {
            body.put("temperature", temperature);
        }
        if (topP != null) {
            body.put("top_p", topP);
        }
        if (tools != null && !tools.isEmpty()) { // Added null check for safety
            JSONArray toolArr = new JSONArray();
            for (var t : tools) {
                toolArr.put(t.toJson());
            }
            body.put("tools", toolArr);
        }
        if (toolChoice != null) {
            body.put("tool_choice", toolChoice);
        }
        // Removed parallel_tool_calls serialization [cite: 18]
        if (logprobs != null) {
            body.put("logprobs", logprobs);
        }
        if (topLogprobs != null) {
            body.put("top_logprobs", topLogprobs);
        }

        return body;
    }

    @Override
    public DeepSeekCompletionResponse createResponse(String responseBody) {
        return new DeepSeekCompletionResponse(new JSONObject(responseBody), this);
    }

    public static Builder builder(DeepSeekClient client) {
        return new Builder(client);
    }

    // Added DeepSeekStreamOptions class definition as requested [cite: 7]
    /**
     * Represents the options for streaming responses.
     * Currently only supports 'include_usage'.
     */
    public static final class DeepSeekStreamOptions {
        private final Boolean includeUsage;

        public DeepSeekStreamOptions(Boolean includeUsage) { // [cite: 10]
            this.includeUsage = includeUsage;
        }

        public JSONObject toJson() { // [cite: 11]
            JSONObject obj = new JSONObject();
            if (includeUsage != null) { // [cite: 12]
                obj.put("include_usage", includeUsage); // [cite: 13]
            }
            return obj; // [cite: 14]
        }

        public Boolean includeUsage() { // [cite: 15]
            return includeUsage;
        }

        // Optional: Static factory method [cite: 16]
        public static DeepSeekStreamOptions withUsage(boolean include) {
            return new DeepSeekStreamOptions(include);
        }
    }

    public static final class Builder extends ApiRequestBuilderBase<Builder, DeepSeekCompletionRequest> {
        private final DeepSeekClient client;
        private String model;
        private final List<JSONObject> messages = new ArrayList<>();
        private Double frequencyPenalty;
        private Integer maxTokens;
        private Double presencePenalty;
        private DeepSeekResponseFormat responseFormat;
        private Object stop;
        private Boolean stream;
        // Changed type from JSONObject to DeepSeekStreamOptions [cite: 8]
        private DeepSeekStreamOptions streamOptions;
        private Double temperature;
        private Double topP;
        private final List<DeepSeekToolDefinition> tools = new ArrayList<>();
        private Object toolChoice;
        // Removed parallelToolCalls field [cite: 17]
        private Boolean logprobs;
        private Integer topLogprobs;

        public Builder(DeepSeekClient client) {
            this.client = client;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        // Added overloaded method with name parameter [cite: 1, 2]
        public Builder addSystemMessage(String content, String name) {
            JSONObject message = new JSONObject().put("role", "system").put("content", content);
            if (name != null && !name.isBlank()) { // [cite: 4]
                message.put("name", name);
            }
            messages.add(message); // [cite: 5]
            return this;
        }

        // Original method calling the new overloaded one [cite: 6]
        public Builder addSystemMessage(String content) {
            return addSystemMessage(content, null);
        }

        // Added overloaded method with name parameter [cite: 1, 2, 3]
        public Builder addUserMessage(String content, String name) {
            JSONObject message = new JSONObject().put("role", "user").put("content", content);
            if (name != null && !name.isBlank()) { // [cite: 4]
                message.put("name", name);
            }
            messages.add(message); // [cite: 5]
            return this;
        }

        // Original method calling the new overloaded one [cite: 6]
        public Builder addUserMessage(String content) {
            return addUserMessage(content, null);
        }

        // Added overloaded method with name parameter [cite: 1, 2]
        public Builder addAssistantMessage(String content, String name) {
            JSONObject message = new JSONObject().put("role", "assistant").put("content", content);
            if (name != null && !name.isBlank()) { // [cite: 4]
                message.put("name", name);
            }
            messages.add(message); // [cite: 5]
            return this;
        }

        // Original method calling the new overloaded one [cite: 6]
        public Builder addAssistantMessage(String content) {
            return addAssistantMessage(content, null);
        }

        // Added overloaded method with name parameter for tool messages
        // (Assuming tool messages might also need a name, although not explicitly mentioned for this file)
        public Builder addToolMessage(String content, String toolCallId, String name) {
            JSONObject message = new JSONObject()
                    .put("role", "tool")
                    .put("content", content)
                    .put("tool_call_id", toolCallId); // Assuming tool_call_id is needed
            if (name != null && !name.isBlank()) {
                message.put("name", name);
            }
            messages.add(message);
            return this;
        }

        public Builder addToolMessage(String content, String toolCallId) {
            return addToolMessage(content, toolCallId, null);
        }


        public Builder addAllMessages(List<JSONObject> msgs) {
            this.messages.addAll(msgs);
            return this;
        }

        public Builder frequencyPenalty(Double freq) {
            this.frequencyPenalty = freq;
            return this;
        }

        public Builder maxTokens(Integer m) {
            this.maxTokens = m;
            return this;
        }

        public Builder presencePenalty(Double pres) {
            this.presencePenalty = pres;
            return this;
        }

        public Builder responseFormat(DeepSeekResponseFormat rf) {
            this.responseFormat = rf;
            return this;
        }

        public Builder stop(Object stopValue) {
            this.stop = stopValue;
            return this;
        }

        public Builder stream(Boolean s) {
            this.stream = s;
            return this;
        }

        // Changed parameter type from JSONObject to DeepSeekStreamOptions [cite: 8]
        public Builder streamOptions(DeepSeekStreamOptions opts) {
            this.streamOptions = opts;
            return this;
        }

        public Builder temperature(Double t) {
            this.temperature = t;
            return this;
        }

        public Builder topP(Double tp) {
            this.topP = tp;
            return this;
        }

        public Builder addTool(DeepSeekToolDefinition tool) {
            this.tools.add(tool);
            return this;
        }

        public Builder addAllTools(List<DeepSeekToolDefinition> t) {
            this.tools.addAll(t);
            return this;
        }

        public Builder toolChoice(Object tc) {
            this.toolChoice = tc;
            return this;
        }

        // Removed parallelToolCalls() builder method [cite: 18]

        public Builder logprobs(Boolean log) {
            this.logprobs = log;
            return this;
        }

        public Builder topLogprobs(Integer top) {
            this.topLogprobs = top;
            return this;
        }

        public DeepSeekCompletionRequest build() {
            // Ensure tools is not null before passing to List.copyOf
            List<DeepSeekToolDefinition> finalTools = tools == null ? List.of() : List.copyOf(tools);
            List<JSONObject> finalMessages = messages == null ? List.of() : List.copyOf(messages);

            return new DeepSeekCompletionRequest(
                    this,
                    model,
                    finalMessages,
                    frequencyPenalty,
                    maxTokens,
                    presencePenalty,
                    responseFormat,
                    stop,
                    stream,
                    streamOptions,
                    temperature,
                    topP,
                    finalTools,
                    toolChoice,
                    // Removed parallelToolCalls from build() call [cite: 17]
                    logprobs,
                    topLogprobs
            );
        }

        @Override
        public DeepSeekCompletionResponse executeWithExponentialBackoff() {
            return new DeepSeekCompletionCallHandler(client).handleRequest(build(), true);
        }

        @Override
        public DeepSeekCompletionResponse execute() {
            return new DeepSeekCompletionCallHandler(client).handleRequest(build(), false);
        }
    }
}