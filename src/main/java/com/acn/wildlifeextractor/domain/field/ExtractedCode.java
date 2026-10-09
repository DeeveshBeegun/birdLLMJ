package com.acn.wildlifeextractor.domain.field;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * A single extracted vocabulary-controlled value.
 *
 * @param <T>            the enum type that governs the allowed values for this field
 * @param vocabularyName the constant name of the controlling vocabulary; the model must set
 *                       this to the vocabulary declared for the field, and it is validated
 *                       against the schema and vocabulary stages
 */
public record ExtractedCode<T extends Enum<T>>(
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

    /**
     * Resolves the normalizedValue to its enum constant.
     * Returns empty if the field is not PRESENT, the value is blank, or it doesn't match any
     * constant in {@code enumClass}.
     */
    public Optional<T> resolve(Class<T> enumClass) {
        if (status != ExtractionFieldStatus.PRESENT || normalizedValue == null || normalizedValue.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Enum.valueOf(enumClass,
                    normalizedValue.toUpperCase(Locale.ROOT).replace(' ', '_')));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
