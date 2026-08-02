package com.acn.wildlifeextractor.application.error;

/**
 * The schema-valid JSON could not be mapped onto the typed extraction DTO (for example an unknown
 * property survived, or a value had the wrong shape). Structurally repairable.
 */
public class ModelOutputMappingException extends ExtractionException {

    public ModelOutputMappingException(String message, Throwable cause) {
        super("MODEL_OUTPUT_MAPPING_FAILED", message, cause);
    }
}
