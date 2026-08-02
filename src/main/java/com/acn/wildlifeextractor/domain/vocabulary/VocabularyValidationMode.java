package com.acn.wildlifeextractor.domain.vocabulary;

/**
 * How strictly a vocabulary validates spoken values.
 */
public enum VocabularyValidationMode {
    /** Any non-blank value is accepted; unknown values produce a warning. */
    OPEN,
    /** Only listed canonical values or deterministic aliases are accepted. */
    CLOSED
}
