package com.acn.wildlifeextractor.application.error;

/** Structural correction attempts were exhausted without producing valid output. */
public class CorrectionExhaustedException extends ExtractionException {

    public CorrectionExhaustedException(String message) {
        super("CORRECTION_EXHAUSTED", message);
    }
}
