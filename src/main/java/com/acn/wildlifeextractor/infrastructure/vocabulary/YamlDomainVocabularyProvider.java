package com.acn.wildlifeextractor.infrastructure.vocabulary;

import java.util.Map;
import java.util.Optional;

import com.acn.wildlifeextractor.domain.vocabulary.DomainVocabularyProvider;
import com.acn.wildlifeextractor.domain.vocabulary.UnknownVocabularyException;
import com.acn.wildlifeextractor.domain.vocabulary.VocabularyDefinition;
import org.springframework.stereotype.Component;

/**
 * Loads all production vocabularies from {@code classpath*:vocabularies/*.yml} at startup and
 * exposes them immutably. A structurally invalid vocabulary aborts application startup.
 */
@Component
public class YamlDomainVocabularyProvider implements DomainVocabularyProvider {

    private static final String LOCATION_PATTERN = "classpath*:vocabularies/*.yml";

    private final Map<String, VocabularyDefinition> definitions;

    public YamlDomainVocabularyProvider() {
        this.definitions = new VocabularyDefinitionLoader().load(LOCATION_PATTERN);
    }

    @Override
    public VocabularyDefinition getRequired(String vocabularyName) {
        VocabularyDefinition definition = definitions.get(vocabularyName);
        if (definition == null) {
            throw new UnknownVocabularyException(vocabularyName);
        }
        return definition;
    }

    @Override
    public Optional<VocabularyDefinition> find(String vocabularyName) {
        return Optional.ofNullable(definitions.get(vocabularyName));
    }

    public int size() {
        return definitions.size();
    }
}
