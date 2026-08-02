package com.acn.wildlifeextractor.application.error;

/** A confirmation could not be completed (for example a rejected result cannot be confirmed). */
public class ExtractionConfirmationException extends ExtractionException {

    public ExtractionConfirmationException(String message) {
        super("EXTRACTION_CONFIRMATION_FAILED", message);
    }
}
