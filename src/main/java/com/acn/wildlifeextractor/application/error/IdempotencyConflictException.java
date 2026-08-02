package com.acn.wildlifeextractor.application.error;

/** The same request id was reused with a different request fingerprint. */
public class IdempotencyConflictException extends ExtractionException {

    public IdempotencyConflictException(String requestId) {
        super("IDEMPOTENCY_CONFLICT", "Request id " + requestId + " was reused with different content");
    }
}
