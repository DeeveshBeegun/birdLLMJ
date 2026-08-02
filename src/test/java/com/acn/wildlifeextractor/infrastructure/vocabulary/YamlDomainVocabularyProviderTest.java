package com.acn.wildlifeextractor.infrastructure.vocabulary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acn.wildlifeextractor.domain.vocabulary.UnknownVocabularyException;
import org.junit.jupiter.api.Test;

class YamlDomainVocabularyProviderTest {

    private final YamlDomainVocabularyProvider provider = new YamlDomainVocabularyProvider();

    @Test
    void loadsAllProductionVocabularies() {
        assertThat(provider.size()).isEqualTo(79);
    }

    @Test
    void returnsRegisteredVocabulary() {
        assertThat(provider.find("BirdSex")).isPresent();
        assertThat(provider.getRequired("BirdSex").name()).isEqualTo("BirdSex");
    }

    @Test
    void throwsForUnknownRequiredVocabulary() {
        assertThat(provider.find("DoesNotExist")).isEmpty();
        assertThatThrownBy(() -> provider.getRequired("DoesNotExist"))
                .isInstanceOf(UnknownVocabularyException.class);
    }
}
