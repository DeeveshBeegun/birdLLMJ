package com.acn.wildlifeextractor.domain.field;

import java.util.List;

import com.acn.wildlifeextractor.domain.reference.ReferenceResolutionStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A spoken reference to a trusted entity (species, bird, nest, user, ...).
 *
 * <p>The model may populate only {@code spokenValue}, {@code referenceType}, {@code lookupKey},
 * {@code status} and {@code evidence}. The resolved fields, {@code resolutionStatus} and
 * {@code candidates} are set exclusively by deterministic application code.</p>
 *
 * <p>{@code ignoreUnknown} tolerates the scalar-shaped keys (rawValue/normalizedValue) that small
 * models frequently add to reference fields; they are ignored, and the spoken value is read from
 * {@code spokenValue}/{@code lookupKey}.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExtractedReference(
        String spokenValue,
        String referenceType,
        String lookupKey,
        String resolvedExternalId,
        String resolvedDisplayName,
        ReferenceResolutionStatus resolutionStatus,
        ExtractionFieldStatus status,
        String evidence,
        List<ReferenceCandidate> candidates,
        List<String> warnings
) {
    public ExtractedReference {
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
