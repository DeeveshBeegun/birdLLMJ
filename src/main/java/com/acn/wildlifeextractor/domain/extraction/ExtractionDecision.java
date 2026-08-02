package com.acn.wildlifeextractor.domain.extraction;

/**
 * The deterministic acceptance decision computed by the application after the full
 * validation pipeline has run. The model never selects this value.
 */
public enum ExtractionDecision {
    ACCEPT_AUTOMATICALLY,
    REQUEST_MORE_INFORMATION,
    MANUAL_REVIEW,
    REJECT
}
