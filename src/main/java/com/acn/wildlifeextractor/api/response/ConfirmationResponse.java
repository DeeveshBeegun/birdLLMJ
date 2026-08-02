package com.acn.wildlifeextractor.api.response;

import java.time.Instant;

import com.acn.wildlifeextractor.domain.extraction.ExtractionDecision;

/**
 * Result of confirming a stored extraction.
 */
public record ConfirmationResponse(
        String requestId,
        ExtractionDecision decision,
        boolean confirmed,
        Instant confirmedAt
) {
}
