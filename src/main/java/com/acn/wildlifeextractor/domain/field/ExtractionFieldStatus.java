package com.acn.wildlifeextractor.domain.field;

/**
 * The extraction outcome for a single field.
 */
public enum ExtractionFieldStatus {
    /** A value was explicitly present in the transcript and normalized. */
    PRESENT,
    /** No value for this field was present in the transcript. */
    MISSING,
    /** A value was mentioned but was unclear or conflicting. */
    AMBIGUOUS,
    /** A value was present but could not be normalized to a valid form. */
    INVALID,
    /** The field does not apply in the observed context. */
    NOT_APPLICABLE,
    /** A reference value was spoken but could not be deterministically resolved. */
    UNRESOLVED_REFERENCE
}
