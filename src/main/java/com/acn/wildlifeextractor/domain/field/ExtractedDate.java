package com.acn.wildlifeextractor.domain.field;

import java.time.LocalDate;
import java.util.List;

/**
 * A single extracted calendar date. {@code normalizedValue} is a nullable {@link LocalDate}
 * so a missing date is never represented by a placeholder.
 */
public record ExtractedDate(
        String rawValue,
        LocalDate normalizedValue,
        ExtractionFieldStatus status,
        String evidence,
        List<String> warnings
) {
    public ExtractedDate {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
