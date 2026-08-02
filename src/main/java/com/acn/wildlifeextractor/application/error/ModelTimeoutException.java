package com.acn.wildlifeextractor.application.error;

/**
 * The model did not respond within the configured execution timeout.
 */
public class ModelTimeoutException extends ExtractionException {

    public ModelTimeoutException(String message, Throwable cause) {
        super("MODEL_TIMEOUT", message, cause);
    }

    public ModelTimeoutException(String message) {
        super("MODEL_TIMEOUT", message);
    }
}
