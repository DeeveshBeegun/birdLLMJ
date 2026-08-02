package com.acn.wildlifeextractor.domain.field;

import java.util.List;

/**
 * A single extracted integer value. {@code normalizedValue} is a nullable {@link Integer}
 * so that a genuinely missing value is distinguishable from zero.
 */
public record ExtractedInteger(
        String rawValue,
        Integer normalizedValue,
        ExtractionFieldStatus status,
        String evidence,
        List<String> warnings
) {
    public ExtractedInteger {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
