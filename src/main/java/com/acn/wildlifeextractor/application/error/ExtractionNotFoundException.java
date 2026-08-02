package com.acn.wildlifeextractor.application.error;

/** No stored extraction exists for the given request id. */
public class ExtractionNotFoundException extends ExtractionException {

    public ExtractionNotFoundException(String requestId) {
        super("EXTRACTION_NOT_FOUND", "No extraction found for request id " + requestId);
    }
}
