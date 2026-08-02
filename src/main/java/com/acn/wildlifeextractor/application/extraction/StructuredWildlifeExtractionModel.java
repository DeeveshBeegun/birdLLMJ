package com.acn.wildlifeextractor.application.extraction;

/**
 * Provider-neutral boundary to a structured-output language model. Implementations live in the
 * infrastructure layer; the application and domain never depend on a specific AI provider.
 */
public interface StructuredWildlifeExtractionModel {

    RawModelExtractionResponse extract(WildlifeModelExtractionRequest request);
}
