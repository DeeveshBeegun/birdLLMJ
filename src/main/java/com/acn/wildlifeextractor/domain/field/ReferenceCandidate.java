package com.acn.wildlifeextractor.domain.field;

import java.util.Map;

/**
 * A candidate match produced by deterministic reference resolution. Only application code
 * populates candidates; the model never does.
 *
 * @param externalId     the trusted external identifier of the candidate
 * @param displayName    a human-readable label safe to surface
 * @param score          the match score, higher is better
 * @param safeAttributes non-sensitive attributes safe to expose in a preview
 */
public record ReferenceCandidate(
        String externalId,
        String displayName,
        double score,
        Map<String, String> safeAttributes
) {
    public ReferenceCandidate {
        safeAttributes = safeAttributes == null ? Map.of() : Map.copyOf(safeAttributes);
    }
}
