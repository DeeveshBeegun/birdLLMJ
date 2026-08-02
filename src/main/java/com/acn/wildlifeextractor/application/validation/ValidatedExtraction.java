package com.acn.wildlifeextractor.application.validation;

import java.util.List;

import com.acn.wildlifeextractor.domain.extraction.ExtractionDecision;
import com.acn.wildlifeextractor.domain.extraction.ExtractionStatus;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The safe, validated outcome of the extraction pipeline for a single request: the validated
 * fields tree, the recorded finding collections and the deterministic decision. Raw model output
 * is intentionally absent.
 */
public record ValidatedExtraction(
        WildlifeFormType formType,
        String schemaVersion,
        String promptVersion,
        ExtractionStatus extractionStatus,
        JsonNode validatedFields,
        List<String> missingRequiredFields,
        List<String> ambiguousFields,
        List<String> invalidFields,
        List<String> unresolvedReferences,
        List<String> warnings,
        ExtractionDecision decision
) {
    public ValidatedExtraction {
        missingRequiredFields = List.copyOf(missingRequiredFields);
        ambiguousFields = List.copyOf(ambiguousFields);
        invalidFields = List.copyOf(invalidFields);
        unresolvedReferences = List.copyOf(unresolvedReferences);
        warnings = List.copyOf(warnings);
    }
}
