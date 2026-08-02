package com.acn.wildlifeextractor.domain.field;

import java.util.List;

/**
 * A single extracted list of free-text values. A genuinely absent list is represented with
 * status {@code MISSING} and empty value lists, never with a fabricated placeholder entry.
 */
public record ExtractedStringList(
        List<String> rawValues,
        List<String> normalizedValues,
        ExtractionFieldStatus status,
        String evidence,
        List<String> warnings
) {
    public ExtractedStringList {
        rawValues = rawValues == null ? List.of() : List.copyOf(rawValues);
        normalizedValues = normalizedValues == null ? List.of() : List.copyOf(normalizedValues);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
