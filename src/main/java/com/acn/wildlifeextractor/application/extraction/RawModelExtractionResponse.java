package com.acn.wildlifeextractor.application.extraction;

import java.time.Duration;
import java.util.Map;

/**
 * The raw, untrusted text returned by the model together with safe, low-cardinality metadata.
 * The content is not parsed or trusted here; downstream stages parse and validate it.
 */
public record RawModelExtractionResponse(
        String content,
        String provider,
        String model,
        Duration duration,
        Map<String, Object> safeMetadata
) {
    public RawModelExtractionResponse {
        safeMetadata = safeMetadata == null ? Map.of() : Map.copyOf(safeMetadata);
    }
}
