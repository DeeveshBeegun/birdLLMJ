package com.acn.wildlifeextractor.domain.extraction;

import java.util.List;

import com.acn.wildlifeextractor.domain.form.WildlifeFormType;

/**
 * The shared envelope every form-specific model result is wrapped in. The {@code fields}
 * payload is the form-specific extraction DTO. The field-name collections are advisory
 * summaries produced by the model; the authoritative summary is recomputed deterministically
 * during validation.
 *
 * @param <T> the form-specific fields type
 */
public record FormExtractionEnvelope<T>(
        String schemaVersion,
        WildlifeFormType formType,
        ExtractionStatus extractionStatus,
        T fields,
        List<String> missingRequiredFields,
        List<String> ambiguousFields,
        List<String> invalidFields,
        List<String> unresolvedReferences,
        List<String> warnings
) {
    public FormExtractionEnvelope {
        missingRequiredFields = missingRequiredFields == null ? List.of() : List.copyOf(missingRequiredFields);
        ambiguousFields = ambiguousFields == null ? List.of() : List.copyOf(ambiguousFields);
        invalidFields = invalidFields == null ? List.of() : List.copyOf(invalidFields);
        unresolvedReferences = unresolvedReferences == null ? List.of() : List.copyOf(unresolvedReferences);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
