package de.entwicklertraining.deepseek4j.exceptions;

import java.io.Serial;

/**
 * Custom runtime exception for errors related to the DeepSeekTokenService.
 */
public class DeepSeekTokenServiceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public DeepSeekTokenServiceException(String message) {
        super(message);
    }

    public DeepSeekTokenServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
