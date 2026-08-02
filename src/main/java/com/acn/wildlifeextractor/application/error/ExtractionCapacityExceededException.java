package com.acn.wildlifeextractor.application.error;

/** The service is at capacity: too many concurrent or pending model calls. */
public class ExtractionCapacityExceededException extends ExtractionException {

    public ExtractionCapacityExceededException(String message) {
        super("EXTRACTION_CAPACITY_EXCEEDED", message);
    }
}
