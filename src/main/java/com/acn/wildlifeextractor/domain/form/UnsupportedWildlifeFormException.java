package com.acn.wildlifeextractor.domain.form;

/**
 * Thrown when a form type is requested for which no definition is registered.
 */
public class UnsupportedWildlifeFormException extends RuntimeException {

    public UnsupportedWildlifeFormException(WildlifeFormType formType) {
        super("Unsupported wildlife form type: " + formType);
    }

    public UnsupportedWildlifeFormException(String message) {
        super(message);
    }
}
