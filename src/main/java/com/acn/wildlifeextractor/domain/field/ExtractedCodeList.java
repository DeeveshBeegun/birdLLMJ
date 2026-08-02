package com.acn.wildlifeextractor.domain.field;

import java.util.List;

/**
 * A single extracted list of vocabulary-controlled values, such as behaviours or actions.
 *
 * @param vocabularyName the constant name of the controlling vocabulary for every element
 */
public record ExtractedCodeList(
        List<String> rawValues,
        List<String> normalizedValues,
        String vocabularyName,
        ExtractionFieldStatus status,
        String evidence,
        List<String> warnings
) {
    public ExtractedCodeList {
        rawValues = rawValues == null ? List.of() : List.copyOf(rawValues);
        normalizedValues = normalizedValues == null ? List.of() : List.copyOf(normalizedValues);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
