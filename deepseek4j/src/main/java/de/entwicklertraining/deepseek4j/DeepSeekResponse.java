package de.entwicklertraining.deepseek4j;

import de.entwicklertraining.api.base.ApiResponse;
import org.json.JSONObject;

/**
 * A generic response abstraction from DeepSeek calls (chat completions, etc.).
 *
 * Typically, implementations will parse the JSON and
 * offer helper methods to extract data.
 */
public abstract class DeepSeekResponse<T extends DeepSeekRequest<?>> extends ApiResponse<T> {

    protected final JSONObject json;
    private final T request;

    protected DeepSeekResponse(JSONObject json, T request) {
        super(request);
        this.json = json;
        this.request = request;
    }

    public JSONObject getJson() {
        return json;
    }

    public T getRequest() {
        return request;
    }
}
