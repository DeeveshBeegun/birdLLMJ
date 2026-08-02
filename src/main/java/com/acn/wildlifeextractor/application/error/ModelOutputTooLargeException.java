package com.acn.wildlifeextractor.application.error;

/**
 * The model returned a response larger than the configured maximum. Enforced at the model
 * boundary so oversized, potentially adversarial output is rejected before parsing.
 */
public class ModelOutputTooLargeException extends ExtractionException {

    public ModelOutputTooLargeException(int actualLength, int maximumLength) {
        super("MODEL_OUTPUT_TOO_LARGE",
                "Model response length " + actualLength + " exceeds maximum " + maximumLength);
    }
}
