package com.acn.wildlifeextractor.application.error;

/**
 * Base type for all extraction pipeline exceptions. Every subtype carries a stable error code
 * used for safe API error responses and low-cardinality metrics.
 */
public abstract class ExtractionException extends RuntimeException {

    private final String errorCode;

    protected ExtractionException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected ExtractionException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }
}
