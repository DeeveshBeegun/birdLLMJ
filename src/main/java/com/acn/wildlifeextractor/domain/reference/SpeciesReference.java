package com.acn.wildlifeextractor.domain.reference;

/**
 * A trusted species record. Identifiers and attributes originate from the reference source, never
 * from the model.
 */
public record SpeciesReference(
        String externalId,
        String speciesKey,
        String commonName,
        String latinName,
        String description,
        Boolean indigenous,
        String categoryName,
        String feedingTrait
) {
}
