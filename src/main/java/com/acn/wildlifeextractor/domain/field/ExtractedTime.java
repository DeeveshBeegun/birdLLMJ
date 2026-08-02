package com.acn.wildlifeextractor.domain.field;

import java.time.LocalTime;
import java.util.List;

/**
 * A single extracted wall-clock time. {@code normalizedValue} is a nullable {@link LocalTime}
 * so a missing time is never represented by midnight.
 */
public record ExtractedTime(
        String rawValue,
        LocalTime normalizedValue,
        ExtractionFieldStatus status,
        String evidence,
        List<String> warnings
) {
    public ExtractedTime {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
