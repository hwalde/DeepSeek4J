package de.entwicklertraining.deepseek4j.user.balance;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.deepseek4j.DeepSeekClient;
import de.entwicklertraining.deepseek4j.DeepSeekRequest;
import org.json.JSONObject;

/**
 * Represents a request to the DeepSeek User Balance API.
 * This endpoint retrieves the current balance information for the authenticated user.
 */
public final class DeepSeekUserBalanceRequest extends DeepSeekRequest<DeepSeekUserBalanceResponse> {

    DeepSeekUserBalanceRequest(Builder builder) {
        super(builder);
    }

    @Override
    public String getRelativeUrl() {
        return "/user/balance";
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
    public DeepSeekUserBalanceResponse createResponse(String responseBody) {
        return new DeepSeekUserBalanceResponse(new JSONObject(responseBody), this);
    }

    public static Builder builder(DeepSeekClient client) {
        return new Builder(client);
    }

    public static final class Builder extends ApiRequestBuilderBase<Builder, DeepSeekUserBalanceRequest> {
        private final DeepSeekClient client;

        public Builder(DeepSeekClient client) {
            this.client = client;
        }

        @Override
        public DeepSeekUserBalanceRequest build() {
            return new DeepSeekUserBalanceRequest(this);
        }

        @Override
        public DeepSeekUserBalanceResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }

        @Override
        public DeepSeekUserBalanceResponse execute() {
            return client.sendRequest(build());
        }
    }
}