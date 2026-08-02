package com.acn.wildlifeextractor.application.error;

/**
 * The model returned output that is blank or cannot be parsed as JSON.
 */
public class MalformedModelOutputException extends ExtractionException {

    public MalformedModelOutputException(String message) {
        super("MALFORMED_MODEL_OUTPUT", message);
    }

    public MalformedModelOutputException(String message, Throwable cause) {
        super("MALFORMED_MODEL_OUTPUT", message, cause);
    }
}
