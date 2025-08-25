package de.entwicklertraining.deepseek4j.models;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.deepseek4j.DeepSeekClient;
import de.entwicklertraining.deepseek4j.DeepSeekRequest;
import org.json.JSONObject;

/**
 * Represents a request to the DeepSeek Models API.
 * This endpoint lists the currently available models and provides basic information about each one.
 */
public final class DeepSeekModelsRequest extends DeepSeekRequest<DeepSeekModelsResponse> {

    DeepSeekModelsRequest(Builder builder) {
        super(builder);
    }

    @Override
    public String getRelativeUrl() {
        return "/models";
    }

    @Override
    public String getHttpMethod() {
        return "GET";
    }

    @Override
    public String getBody() {
        // GET request doesn't have a body
        return null;
    }

    @Override
    public DeepSeekModelsResponse createResponse(String responseBody) {
        return new DeepSeekModelsResponse(new JSONObject(responseBody), this);
    }

    public static Builder builder(DeepSeekClient deepSeekClient) {
        return new Builder(deepSeekClient);
    }

    public static final class Builder extends ApiRequestBuilderBase<Builder, DeepSeekModelsRequest> {
        private final DeepSeekClient deepSeekClient;

        public Builder(DeepSeekClient deepSeekClient) {
            this.deepSeekClient = deepSeekClient;
        }

        @Override
        public DeepSeekModelsRequest build() {
            return new DeepSeekModelsRequest(this);
        }

        @Override
        public DeepSeekModelsResponse execute() {
            return this.deepSeekClient.sendRequest(build());
        }

        @Override
        public DeepSeekModelsResponse executeWithExponentialBackoff() {
            return this.deepSeekClient.sendRequestWithExponentialBackoff(build());
        }
    }
}