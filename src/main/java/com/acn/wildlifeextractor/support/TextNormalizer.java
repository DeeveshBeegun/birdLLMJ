package com.acn.wildlifeextractor.support;

import java.util.Locale;

/**
 * Deterministic, side-effect-free text normalization shared by vocabulary resolution and
 * evidence matching. Normalization is case-insensitive and whitespace-collapsing.
 */
public final class TextNormalizer {

    private TextNormalizer() {
    }

    /**
     * Trims, collapses internal whitespace runs to a single space, and lower-cases using the
     * root locale. Returns an empty string for a {@code null} input.
     */
    public static String normalizeForMatch(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * True when the value is {@code null} or contains only whitespace.
     */
    public static boolean isBlank(String value) {
        return value == null || value.strip().isEmpty();
    }
}
