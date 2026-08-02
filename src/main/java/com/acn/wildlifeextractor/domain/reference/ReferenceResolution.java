package com.acn.wildlifeextractor.domain.reference;

import java.util.List;

import com.acn.wildlifeextractor.domain.field.ReferenceCandidate;

/**
 * The deterministic outcome of resolving a single spoken reference value.
 */
public record ReferenceResolution(
        ReferenceResolutionStatus status,
        String resolvedExternalId,
        String resolvedDisplayName,
        List<ReferenceCandidate> candidates
) {
    public ReferenceResolution {
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
    }

    public static ReferenceResolution resolved(String externalId, String displayName) {
        return new ReferenceResolution(ReferenceResolutionStatus.RESOLVED, externalId, displayName, List.of());
    }

    public static ReferenceResolution notFound() {
        return new ReferenceResolution(ReferenceResolutionStatus.NOT_FOUND, null, null, List.of());
    }

    public static ReferenceResolution ambiguous(List<ReferenceCandidate> candidates) {
        return new ReferenceResolution(ReferenceResolutionStatus.AMBIGUOUS, null, null, candidates);
    }

    public static ReferenceResolution notRequested() {
        return new ReferenceResolution(ReferenceResolutionStatus.NOT_REQUESTED, null, null, List.of());
    }
}
