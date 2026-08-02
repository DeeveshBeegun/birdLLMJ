package com.acn.wildlifeextractor.domain.reference;

/**
 * A trusted nest reference.
 */
public record NestReference(
        String externalId,
        String nestReference,
        String nestSiteName
) {
}
