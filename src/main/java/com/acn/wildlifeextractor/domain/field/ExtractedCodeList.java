package com.acn.wildlifeextractor.domain.field;

import java.util.List;
import java.util.Locale;

/**
 * A single extracted list of vocabulary-controlled values, such as behaviours or actions.
 *
 * @param <T>            the enum type that governs every element in the list
 * @param vocabularyName the constant name of the controlling vocabulary for every element
 */
public record ExtractedCodeList<T extends Enum<T>>(
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

    /**
     * Resolves each normalizedValue to its enum constant, silently skipping unrecognised values.
     */
    public List<T> resolveList(Class<T> enumClass) {
        return normalizedValues.stream()
                .<T>map(v -> {
                    try {
                        return Enum.valueOf(enumClass,
                                v.toUpperCase(Locale.ROOT).replace(' ', '_'));
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(v -> v != null)
                .toList();
    }
}
