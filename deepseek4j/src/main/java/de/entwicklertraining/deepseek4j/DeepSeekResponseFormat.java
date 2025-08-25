package de.entwicklertraining.deepseek4j;

import org.json.JSONObject;

/**
 * Represents the response_format object for DeepSeek,
 * specifying the format the model must output.
 * E.g. { "type": "json_object" }
 */
public final class DeepSeekResponseFormat {

    /**
     * Possible values: "text" or "json_object"
     */
    private final String type;

    public DeepSeekResponseFormat(String type) {
        this.type = type;
    }

    public static DeepSeekResponseFormat text() {
        return new DeepSeekResponseFormat("text");
    }

    public static DeepSeekResponseFormat jsonObject() {
        return new DeepSeekResponseFormat("json_object");
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", type);
        return obj;
    }

    public String type() {
        return type;
    }
}
