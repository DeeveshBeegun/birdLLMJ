package com.acn.wildlifeextractor.domain.field;

import java.util.List;

/**
 * A single extracted vocabulary-controlled value.
 *
 * @param vocabularyName the constant name of the controlling vocabulary; the model must set
 *                       this to the vocabulary declared for the field, and it is validated
 *                       against the schema and vocabulary stages
 */
public record ExtractedCode(
        String rawValue,
        String normalizedValue,
        String vocabularyName,
        ExtractionFieldStatus status,
        String evidence,
        List<String> warnings
) {
    public ExtractedCode {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
