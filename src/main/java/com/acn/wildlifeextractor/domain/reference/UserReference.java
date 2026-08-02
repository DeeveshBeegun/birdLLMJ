package com.acn.wildlifeextractor.domain.reference;

/**
 * A trusted user reference (for example an observer or ringer).
 */
public record UserReference(
        String externalId,
        String name
) {
}
