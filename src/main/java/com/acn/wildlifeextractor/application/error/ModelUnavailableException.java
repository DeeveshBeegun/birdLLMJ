package com.acn.wildlifeextractor.application.error;

/**
 * The model provider could not be reached or returned a retryable server-side failure.
 */
public class ModelUnavailableException extends ExtractionException {

    public ModelUnavailableException(String message, Throwable cause) {
        super("MODEL_UNAVAILABLE", message, cause);
    }

    public ModelUnavailableException(String message) {
        super("MODEL_UNAVAILABLE", message);
    }
}
