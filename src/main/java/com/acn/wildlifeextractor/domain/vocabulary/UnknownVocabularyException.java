package com.acn.wildlifeextractor.domain.vocabulary;

/**
 * Thrown when a required vocabulary is requested but not registered. This indicates a
 * configuration or form-definition error rather than a request problem.
 */
public class UnknownVocabularyException extends RuntimeException {

    public UnknownVocabularyException(String vocabularyName) {
        super("Unknown vocabulary: " + vocabularyName);
    }
}
