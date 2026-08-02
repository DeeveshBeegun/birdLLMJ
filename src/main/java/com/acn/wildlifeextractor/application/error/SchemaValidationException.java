package com.acn.wildlifeextractor.application.error;

import java.util.List;

import com.acn.wildlifeextractor.domain.validation.SanitizedValidationError;

/**
 * The model output did not satisfy the form's JSON Schema. Carries only sanitized errors so no
 * sensitive instance value is exposed. This failure is structurally repairable and therefore a
 * candidate for a single correction attempt.
 */
public class SchemaValidationException extends ExtractionException {

    private final transient List<SanitizedValidationError> errors;

    public SchemaValidationException(List<SanitizedValidationError> errors) {
        super("SCHEMA_VALIDATION_FAILED", "Model output failed JSON Schema validation");
        this.errors = List.copyOf(errors);
    }

    public List<SanitizedValidationError> errors() {
        return errors;
    }
}
