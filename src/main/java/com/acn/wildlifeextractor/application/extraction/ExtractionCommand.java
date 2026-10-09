package com.acn.wildlifeextractor.application.extraction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
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
        String language,
        String userId,
        LocalTime time,
        LocalDate date,
        BigDecimal latitude,
        BigDecimal longitude,
        String subPopulation
) {
}
