package com.acn.wildlifeextractor.domain.vocabulary;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class VocabularyDefinitionTest {

    private VocabularyDefinition closedBirdSex() {
        return new VocabularyDefinition(
                "BirdSex",
                VocabularyValidationMode.CLOSED,
                Set.of("MALE", "FEMALE", "UNKNOWN"),
                Map.of("male", "MALE", "m", "MALE", "female", "FEMALE"),
                true);
    }

    @Test
    void resolvesExactAllowedValue() {
        assertThat(closedBirdSex().resolveCanonical("MALE")).contains("MALE");
    }

    @Test
    void resolvesAllowedValueCaseInsensitively() {
        assertThat(closedBirdSex().resolveCanonical("  female  ")).contains("FEMALE");
    }

    @Test
    void resolvesDeterministicAlias() {
        assertThat(closedBirdSex().resolveCanonical("m")).contains("MALE");
        assertThat(closedBirdSex().resolveCanonical("MALE")).contains("MALE");
    }

    @Test
    void returnsEmptyForUnknownValue() {
        assertThat(closedBirdSex().resolveCanonical("hermaphrodite")).isEmpty();
    }

    @Test
    void returnsEmptyForBlankValue() {
        assertThat(closedBirdSex().resolveCanonical("   ")).isEmpty();
        assertThat(closedBirdSex().resolveCanonical(null)).isEmpty();
    }

    @Test
    void reportsApprovedValues() {
        VocabularyDefinition definition = closedBirdSex();
        assertThat(definition.isApprovedValue("male")).isTrue();
        assertThat(definition.isApprovedValue("goose")).isFalse();
        assertThat(definition.isClosed()).isTrue();
    }

    @Test
    void collectionsAreImmutable() {
        VocabularyDefinition definition = closedBirdSex();
        assertThat(definition.allowedValues()).isUnmodifiable();
        assertThat(definition.aliases()).isUnmodifiable();
    }
}
