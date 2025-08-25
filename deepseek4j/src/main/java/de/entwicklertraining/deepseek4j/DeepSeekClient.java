package de.entwicklertraining.deepseek4j;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.api.base.ApiClientSettings;
import de.entwicklertraining.deepseek4j.chat.completion.DeepSeekCompletionRequest;
import de.entwicklertraining.deepseek4j.models.DeepSeekModelsRequest;
import de.entwicklertraining.deepseek4j.user.balance.DeepSeekUserBalanceRequest;

/**
 * DeepSeekClient handles HTTP requests to the DeepSeek Chat Completion API,
 * including exponential backoff for 429 (Rate Limit) and 503 (Server Overloaded).
 * <p>
 * Error behavior:
 *  - 400 -> throw HTTP_400_RequestRejectedException
 *  - 401 -> throw HTTP_401_AuthorizationException
 *  - 402 -> throw HTTP_402_PaymentRequiredException
 *  - 422 -> throw HTTP_422_UnprocessableEntityException
 *  - 429 -> attempt exponential backoff; if still not resolved after max tries -> throw HTTP_429_RateLimitOrQuotaException
 *  - 500 -> throw HTTP_500_ServerErrorException
 *  - 503 -> attempt exponential backoff; if still not resolved after max tries -> throw HTTP_503_ServerUnavailableException
 *  - else -> throw ApiClientException
 */
public final class DeepSeekClient extends ApiClient {

    private static DeepSeekClient instance;

    public DeepSeekClient() {
        this(ApiClientSettings.builder().build(), "https://api.deepseek.com");
    }

    public DeepSeekClient(ApiClientSettings settings) {
        this(settings, "https://api.deepseek.com");
    }

    public DeepSeekClient(ApiClientSettings settings, String customBaseUrl) {
        super(settings);

        setBaseUrl(customBaseUrl);

        // if no API key is provided, try to read it from the environment variable
        if(settings.getBearerAuthenticationKey().isEmpty() && System.getenv("DEEPSEEK_API_KEY")!=null) {
            this.settings = this.settings.toBuilder().setBearerAuthenticationKey(System.getenv("DEEPSEEK_API_KEY")).build();
        }

        registerStatusCodeException(400, HTTP_400_RequestRejectedException.class, "Invalid format (HTTP 400):", false);
        registerStatusCodeException(401, HTTP_401_AuthorizationException.class, "Authentication failed (HTTP 401):", false);
        registerStatusCodeException(402, HTTP_402_PaymentRequiredException.class, "Insufficient balance (HTTP 402):", false);
        registerStatusCodeException(422, HTTP_422_UnprocessableEntityException.class, "Invalid parameters (HTTP 422):", false);
        registerStatusCodeException(429, HTTP_429_RateLimitOrQuotaException.class, "Rate limit or quota exceeded (HTTP 429):", true);
        registerStatusCodeException(500, HTTP_500_ServerErrorException.class, "Server encountered an issue (HTTP 500):", false);
        registerStatusCodeException(503, HTTP_503_ServerUnavailableException.class, "Server overloaded (HTTP 503):", true);
    }

    public DeepSeekChat chat() {
        return new DeepSeekChat(this);
    }

    public static class DeepSeekChat {
        private final DeepSeekClient client;

        public DeepSeekChat(DeepSeekClient client) {
            this.client = client;
        }

        public DeepSeekCompletionRequest.Builder completion() {
            return DeepSeekCompletionRequest.builder(client);
        }
    }

    public DeepSeekModelsRequest.Builder models() {
        return DeepSeekModelsRequest.builder(this);
    }

    public DeepSeekUser user() {
        return new DeepSeekUser(this);
    }

    public static class DeepSeekUser {
        private final DeepSeekClient client;

        public DeepSeekUser(DeepSeekClient client) {
            this.client = client;
        }

        public DeepSeekUserBalanceRequest.Builder balance() {
            return DeepSeekUserBalanceRequest.builder(client);
        }
    }
}