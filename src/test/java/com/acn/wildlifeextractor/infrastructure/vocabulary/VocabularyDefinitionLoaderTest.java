package com.acn.wildlifeextractor.infrastructure.vocabulary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import com.acn.wildlifeextractor.domain.vocabulary.VocabularyDefinition;
import org.junit.jupiter.api.Test;

class VocabularyDefinitionLoaderTest {

    private final VocabularyDefinitionLoader loader = new VocabularyDefinitionLoader();

    @Test
    void loadsValidClosedAndOpenVocabularies() {
        Map<String, VocabularyDefinition> definitions = loader.load("classpath*:vocab-valid/*.yml");

        assertThat(definitions).containsKeys("BirdSexClosed", "SightingTypeOpen");

        VocabularyDefinition closed = definitions.get("BirdSexClosed");
        assertThat(closed.isClosed()).isTrue();
        assertThat(closed.required()).isTrue();
        assertThat(closed.resolveCanonical("m")).contains("MALE");
        assertThat(closed.resolveCanonical("Unknown Sex")).contains("UNKNOWN");
        assertThat(closed.resolveCanonical("nonsense")).isEmpty();

        VocabularyDefinition open = definitions.get("SightingTypeOpen");
        assertThat(open.isClosed()).isFalse();
        assertThat(open.resolveCanonical("seen directly")).contains("DIRECT");
        assertThat(open.resolveCanonical("DIRECT")).contains("DIRECT");
    }

    @Test
    void rejectsAmbiguousAlias() {
        assertThatThrownBy(() -> loader.load("classpath*:vocab-ambiguous-alias/*.yml"))
                .isInstanceOf(InvalidVocabularyException.class)
                .hasMessageContaining("ambiguous alias");
    }

    @Test
    void rejectsClosedAliasTargetingNonAllowedValue() {
        assertThatThrownBy(() -> loader.load("classpath*:vocab-closed-bad-alias/*.yml"))
                .isInstanceOf(InvalidVocabularyException.class)
                .hasMessageContaining("targets non-allowed value");
    }

    @Test
    void returnsEmptyMapWhenNoResourcesMatch() {
        assertThat(loader.load("classpath*:vocab-none-here/*.yml")).isEmpty();
    }

    @Test
    void loadsAllProductionVocabularies() {
        Map<String, VocabularyDefinition> definitions = loader.load("classpath*:vocabularies/*.yml");
        assertThat(definitions).containsKeys("BirdSex", "SubPopulation", "Behaviour", "IndividualNumber", "BirdStatus");
        assertThat(definitions).hasSize(79);
        assertThat(definitions.values()).allMatch(v -> !v.isClosed());
    }
}
