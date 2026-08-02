package com.acn.wildlifeextractor.domain.field;

import java.util.List;

/**
 * A single extracted boolean value. {@code normalizedValue} is a nullable {@link Boolean}
 * so that a genuinely missing value is distinguishable from {@code false}.
 */
public record ExtractedBoolean(
        String rawValue,
        Boolean normalizedValue,
        ExtractionFieldStatus status,
        String evidence,
        List<String> warnings
) {
    public ExtractedBoolean {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
