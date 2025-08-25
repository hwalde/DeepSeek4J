package de.entwicklertraining.deepseek4j.models;

import de.entwicklertraining.deepseek4j.DeepSeekResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a response from the DeepSeek Models API.
 * Provides methods to access the list of available models and their information.
 */
public final class DeepSeekModelsResponse extends DeepSeekResponse<DeepSeekModelsRequest> {

    /**
     * Constructs a DeepSeekModelsResponse.
     *
     * @param json    The raw JSON response object.
     * @param request The original request that led to this response.
     */
    public DeepSeekModelsResponse(JSONObject json, DeepSeekModelsRequest request) {
        super(json, request);
    }

    /**
     * Gets the object type, which is always "list".
     * @return The object type string, or null if not present.
     */
    public String getObject() {
        return getJson().optString("object", null);
    }

    /**
     * Gets the list of available models.
     * @return A list of Model objects, or an empty list if not present or invalid.
     */
    public List<Model> getModels() {
        JSONArray dataArray = getJson().optJSONArray("data");
        if (dataArray == null) {
            return Collections.emptyList();
        }
        List<Model> models = new ArrayList<>();
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject modelJson = dataArray.optJSONObject(i);
            if (modelJson != null) {
                models.add(new Model(modelJson));
            }
        }
        return models;
    }

    /**
     * Represents a model in the DeepSeek Models API response.
     */
    public static class Model {
        private final JSONObject json;

        Model(JSONObject json) {
            this.json = (json != null) ? json : new JSONObject();
        }

        /**
         * Gets the model identifier, which can be referenced in the API endpoints.
         * @return The model ID string, or null if not present.
         */
        public String getId() {
            return json.optString("id", null);
        }

        /**
         * Gets the object type, which is always "model".
         * @return The object type string, or null if not present.
         */
        public String getObject() {
            return json.optString("object", null);
        }

        /**
         * Gets the organization that owns the model.
         * @return The organization name string, or null if not present.
         */
        public String getOwnedBy() {
            return json.optString("owned_by", null);
        }
    }
}