package com.acn.wildlifeextractor.domain.reference;

/**
 * A trusted competitor-species record.
 */
public record CompetitorSpeciesReference(
        String externalId,
        String name
) {
}
