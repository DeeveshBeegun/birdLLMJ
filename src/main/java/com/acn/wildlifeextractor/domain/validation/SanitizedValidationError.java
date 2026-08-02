package com.acn.wildlifeextractor.domain.validation;

/**
 * A schema-validation error stripped of any sensitive instance values. Only structural
 * metadata (a stable code, the JSON path, the failing keyword and a safe description) is
 * retained so that errors can be surfaced without leaking transcript-derived content.
 */
public record SanitizedValidationError(
        String errorCode,
        String jsonPath,
        String validationKeyword,
        String safeDescription
) {
}
