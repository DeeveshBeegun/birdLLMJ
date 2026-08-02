package com.acn.wildlifeextractor.application.extraction;

/**
 * Why the model is being invoked. Used to select the correct prompt and to keep initial
 * extraction and structural correction attempts distinct in metrics and counters.
 */
public enum ModelInvocationPurpose {
    INITIAL_EXTRACTION,
    STRUCTURAL_CORRECTION
}
