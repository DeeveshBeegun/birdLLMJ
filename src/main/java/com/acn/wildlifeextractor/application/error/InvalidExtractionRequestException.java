package com.acn.wildlifeextractor.application.error;

/** The inbound API request is invalid (blank/oversized transcript, bad timestamp, etc.). */
public class InvalidExtractionRequestException extends ExtractionException {

    public InvalidExtractionRequestException(String message) {
        super("INVALID_EXTRACTION_REQUEST", message);
    }
}
