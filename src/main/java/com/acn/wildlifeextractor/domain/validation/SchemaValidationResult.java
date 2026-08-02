package com.acn.wildlifeextractor.domain.validation;

import java.util.List;

/**
 * The outcome of validating structured model output against a form's JSON Schema.
 */
public record SchemaValidationResult(
        boolean valid,
        List<SanitizedValidationError> errors
) {
    public SchemaValidationResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static SchemaValidationResult success() {
        return new SchemaValidationResult(true, List.of());
    }

    public static SchemaValidationResult failure(List<SanitizedValidationError> errors) {
        return new SchemaValidationResult(false, errors);
    }
}
