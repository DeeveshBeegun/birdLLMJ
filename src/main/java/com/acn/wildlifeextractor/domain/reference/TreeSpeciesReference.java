package com.acn.wildlifeextractor.domain.reference;

/**
 * A trusted tree-species record.
 */
public record TreeSpeciesReference(
        String externalId,
        String code,
        String scientificName,
        String status
) {
}
