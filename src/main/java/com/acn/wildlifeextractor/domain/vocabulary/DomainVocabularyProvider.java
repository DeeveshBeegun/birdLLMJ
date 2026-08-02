package com.acn.wildlifeextractor.domain.vocabulary;

import java.util.Optional;

/**
 * Supplies controlled vocabulary definitions to the validation pipeline. Implementations
 * load definitions from a trusted source and expose them immutably.
 */
public interface DomainVocabularyProvider {

    /**
     * Returns the definition for the named vocabulary.
     *
     * @throws com.acn.wildlifeextractor.domain.vocabulary.UnknownVocabularyException
     *         when no vocabulary with the given name is registered
     */
    VocabularyDefinition getRequired(String vocabularyName);

    /**
     * Returns the definition for the named vocabulary, if present.
     */
    Optional<VocabularyDefinition> find(String vocabularyName);
}
