package com.acn.wildlifeextractor.domain.extraction;

/**
 * The overall structural completeness of a single form extraction, as reported by the
 * model inside the response envelope. This is advisory; the authoritative acceptance
 * outcome is the deterministically computed {@link ExtractionDecision}.
 */
public enum ExtractionStatus {
    COMPLETE,
    INCOMPLETE,
    INVALID
}
