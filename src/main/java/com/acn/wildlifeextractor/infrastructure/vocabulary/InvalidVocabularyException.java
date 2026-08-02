package com.acn.wildlifeextractor.infrastructure.vocabulary;

/**
 * Thrown at startup when a vocabulary definition is structurally invalid. This deliberately
 * fails application startup rather than deferring the problem to request time.
 */
public class InvalidVocabularyException extends RuntimeException {

    public InvalidVocabularyException(String message) {
        super(message);
    }

    public InvalidVocabularyException(String message, Throwable cause) {
        super(message, cause);
    }
}
