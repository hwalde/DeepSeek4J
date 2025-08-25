package de.entwicklertraining.deepseek4j;

import de.entwicklertraining.api.base.ApiRequest;
import de.entwicklertraining.api.base.ApiRequestBuilderBase;

/**
 * A generic request abstraction for DeepSeek calls (chat completions, etc.).
 *
 * Subclasses must provide:
 *  - the target URI (e.g. https://api.deepseek.com/chat/completions)
 *  - the HTTP method (POST, GET, ...)
 *  - the request body (JSON payload or null)
 *  - a factory method to create the corresponding response object
 */
public abstract class DeepSeekRequest<T extends DeepSeekResponse<?>> extends ApiRequest<T> {

    protected <Y extends ApiRequestBuilderBase<?, ?>> DeepSeekRequest(Y builder) {
        super(builder);
    }

    /**
     * Returns the HTTP method (e.g. "POST" or "GET").
     */
    @Override
    public abstract String getHttpMethod();

    /**
     * Returns the request body as a String (often JSON). May be null if GET.
     */
    @Override
    public abstract String getBody();

    @Override
    public abstract T createResponse(String responseBody);

    /**
     * If the request expects a binary response, override and return true. By default, this is false.
     */
    @Override
    public boolean isBinaryResponse() {
        return false;
    }

    /**
     * If the request expects a binary response, implement this method to produce the DeepSeekResponse from raw bytes.
     */
    @Override
    public T createResponse(byte[] responseBytes) {
        throw new UnsupportedOperationException("This request does not support binary responses.");
    }

    /**
     * By default, we use "application/json".
     */
    @Override
    public String getContentType() {
        return "application/json";
    }

    /**
     * If there's a binary body, override this. Here it's unsupported.
     */
    @Override
    public byte[] getBodyBytes() {
        throw new UnsupportedOperationException("No binary body by default.");
    }
}
