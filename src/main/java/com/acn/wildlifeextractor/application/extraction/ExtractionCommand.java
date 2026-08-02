package com.acn.wildlifeextractor.application.extraction;

import java.time.OffsetDateTime;

import com.acn.wildlifeextractor.domain.form.WildlifeFormType;

/**
 * Internal, validated representation of an extraction request passed to the service.
 */
public record ExtractionCommand(
        String requestId,
        String conversationId,
        WildlifeFormType formType,
        String transcript,
        OffsetDateTime transcriptTimestamp,
        String language
) {
}
