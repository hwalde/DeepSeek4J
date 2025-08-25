package de.entwicklertraining.deepseek4j;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Represents a function-like tool definition in DeepSeek,
 * with a name, description, parameters (JSON schema),
 * and a callback that is executed when the model calls this tool.
 */
public final class DeepSeekToolDefinition {

    private final String name;
    private final String description;
    private final JSONObject parameters;
    private final DeepSeekToolsCallback callback;

    private DeepSeekToolDefinition(String name, String description, JSONObject parameters, DeepSeekToolsCallback callback) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.callback = callback;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public JSONObject parameters() {
        return parameters;
    }

    public DeepSeekToolsCallback callback() {
        return callback;
    }

    /**
     * Produces the JSON structure needed by the DeepSeek API to describe this tool as a "function".
     */
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", "function");

        JSONObject function = new JSONObject();
        function.put("name", name);
        function.put("description", description);
        function.put("parameters", parameters);

        // The docs mention no "strict" field, so we omit it or keep it out.
        tool.put("function", function);
        return tool;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String name;
        private String description;
        private final JSONObject schema = new JSONObject();
        private final JSONObject properties = new JSONObject();
        private final JSONArray required = new JSONArray();
        private DeepSeekToolsCallback callback;
        private boolean areAdditionalPropertiesAllowed = false;

        private Builder(String name) {
            this.name = name;
            schema.put("type", "object");
        }

        public Builder description(String desc) {
            this.description = desc;
            return this;
        }

        public Builder parameter(String paramName, DeepSeekJsonSchema paramSchema, boolean requiredField) {
            properties.put(paramName, paramSchema.toJson());
            if (requiredField) {
                required.put(paramName);
            }
            return this;
        }

        public Builder callback(DeepSeekToolsCallback cb) {
            this.callback = cb;
            return this;
        }

        public Builder allowAdditionalProperties() {
            this.areAdditionalPropertiesAllowed = true;
            return this;
        }

        public DeepSeekToolDefinition build() {
            if (!properties.isEmpty()) {
                schema.put("properties", properties);
            }
            if (!required.isEmpty()) {
                schema.put("required", required);
            }
            schema.put("additionalProperties", areAdditionalPropertiesAllowed);

            return new DeepSeekToolDefinition(name, description, schema, callback);
        }
    }
}
