package com.acn.wildlifeextractor.domain.reference;

/**
 * A trusted bird / ringing-record reference.
 */
public record BirdReference(
        String externalId,
        String birdID,
        String birdName,
        String comboLeft,
        String comboRight,
        String leftRingNumber,
        String rightRingNumber,
        String status,
        String sex
) {
}
