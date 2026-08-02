package com.acn.wildlifeextractor.domain.reference;

/**
 * The outcome of deterministically resolving a spoken reference value against a trusted
 * reference source. Only application code may set this; the model never does.
 */
public enum ReferenceResolutionStatus {
    RESOLVED,
    NOT_FOUND,
    AMBIGUOUS,
    NOT_REQUESTED
}
