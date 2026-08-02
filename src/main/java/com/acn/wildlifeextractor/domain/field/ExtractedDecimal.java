package com.acn.wildlifeextractor.domain.field;

import java.math.BigDecimal;
import java.util.List;

/**
 * A single extracted decimal value. {@code normalizedValue} is a nullable {@link BigDecimal}
 * to preserve precision for coordinates and measurements and to distinguish a missing value
 * from zero.
 */
public record ExtractedDecimal(
        String rawValue,
        BigDecimal normalizedValue,
        ExtractionFieldStatus status,
        String evidence,
        List<String> warnings
) {
    public ExtractedDecimal {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
