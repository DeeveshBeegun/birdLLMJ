package com.acn.wildlifeextractor.domain.vocabulary;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.acn.wildlifeextractor.support.TextNormalizer;

/**
 * An immutable definition of a controlled wildlife vocabulary.
 *
 * <p>Alias keys and allowed values are matched case-insensitively and with collapsed
 * whitespace. An alias maps deterministically to exactly one canonical value.</p>
 *
 * @param name           the vocabulary constant name, e.g. {@code BirdSex}
 * @param validationMode {@link VocabularyValidationMode#OPEN} or {@link VocabularyValidationMode#CLOSED}
 * @param allowedValues  the canonical approved values; may be empty for an initially open vocabulary
 * @param aliases        deterministic spoken-form to canonical-value mappings
 * @param required       whether the vocabulary must always be present for its fields
 */
public record VocabularyDefinition(
        String name,
        VocabularyValidationMode validationMode,
        Set<String> allowedValues,
        Map<String, String> aliases,
        boolean required
) {
    public VocabularyDefinition {
        if (TextNormalizer.isBlank(name)) {
            throw new IllegalArgumentException("Vocabulary name must not be blank");
        }
        if (validationMode == null) {
            throw new IllegalArgumentException("Vocabulary '" + name + "' must declare a validation mode");
        }
        allowedValues = allowedValues == null ? Set.of() : Set.copyOf(allowedValues);
        aliases = aliases == null ? Map.of() : Map.copyOf(aliases);
    }

    /**
     * Resolves a spoken value to its canonical form, if it exactly matches an allowed value
     * (case-insensitively) or a deterministic alias. Returns empty when no deterministic
     * mapping exists.
     */
    public Optional<String> resolveCanonical(String spokenValue) {
        if (TextNormalizer.isBlank(spokenValue)) {
            return Optional.empty();
        }
        String normalized = TextNormalizer.normalizeForMatch(spokenValue);

        for (String allowed : allowedValues) {
            if (TextNormalizer.normalizeForMatch(allowed).equals(normalized)) {
                return Optional.of(allowed);
            }
        }
        for (Map.Entry<String, String> alias : aliases.entrySet()) {
            if (TextNormalizer.normalizeForMatch(alias.getKey()).equals(normalized)) {
                return Optional.of(alias.getValue());
            }
        }
        return Optional.empty();
    }

    /**
     * True when the value is an approved canonical value, matched case-insensitively.
     */
    public boolean isApprovedValue(String value) {
        if (TextNormalizer.isBlank(value)) {
            return false;
        }
        String normalized = TextNormalizer.normalizeForMatch(value);
        return allowedValues.stream()
                .anyMatch(allowed -> TextNormalizer.normalizeForMatch(allowed).equals(normalized));
    }

    public boolean isClosed() {
        return validationMode == VocabularyValidationMode.CLOSED;
    }
}
