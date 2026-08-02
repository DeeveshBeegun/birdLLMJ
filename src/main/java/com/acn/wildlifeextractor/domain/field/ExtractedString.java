package com.acn.wildlifeextractor.domain.field;

import java.util.List;

/**
 * A single extracted free-text value with provenance.
 *
 * @param rawValue        the value exactly as spoken, or {@code null} when missing
 * @param normalizedValue the normalized value, or {@code null} when missing or not applicable
 * @param status          the extraction outcome for this field
 * @param evidence        an exact supporting phrase from the transcript, required when {@code PRESENT}
 * @param warnings        non-fatal notes; never {@code null}
 */
public record ExtractedString(
        String rawValue,
        String normalizedValue,
        ExtractionFieldStatus status,
        String evidence,
        List<String> warnings
) {
    public ExtractedString {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
