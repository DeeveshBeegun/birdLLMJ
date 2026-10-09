package com.acn.wildlifeextractor.application.confirmation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import com.acn.wildlifeextractor.domain.extraction.ExtractionDecision;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The internally stored outcome of an extraction, used for idempotency and for the confirmation
 * flow. Holds a secure fingerprint of the transcript rather than the transcript itself.
 */
public record StoredExtraction(
        String requestId,
        String conversationId,
        WildlifeFormType formType,
        String schemaVersion,
        String promptVersion,
        ExtractionDecision decision,
        JsonNode validatedFields,
        List<String> missingRequiredFields,
        List<String> ambiguousFields,
        List<String> invalidFields,
        List<String> unresolvedReferences,
        List<String> warnings,
        Map<String, Object> safeModelMetadata,
        String modelName,
        int modelCallCount,
        int correctionAttemptCount,
        int infrastructureRetryCount,
        String transcriptFingerprint,
        Instant createdAt,
        Instant confirmedAt,
        boolean confirmed,
        String userId,
        LocalTime time,
        LocalDate date,
        BigDecimal latitude,
        BigDecimal longitude,
        String subPopulation
) {
    public StoredExtraction {
        missingRequiredFields = List.copyOf(missingRequiredFields);
        ambiguousFields = List.copyOf(ambiguousFields);
        invalidFields = List.copyOf(invalidFields);
        unresolvedReferences = List.copyOf(unresolvedReferences);
        warnings = List.copyOf(warnings);
        safeModelMetadata = safeModelMetadata == null ? Map.of() : Map.copyOf(safeModelMetadata);
    }

    public StoredExtraction asConfirmed(Instant confirmedAt) {
        return new StoredExtraction(requestId, conversationId, formType, schemaVersion, promptVersion,
                decision, validatedFields, missingRequiredFields, ambiguousFields, invalidFields,
                unresolvedReferences, warnings, safeModelMetadata, modelName, modelCallCount,
                correctionAttemptCount, infrastructureRetryCount, transcriptFingerprint, createdAt,
                confirmedAt, true, userId, time, date, latitude, longitude, subPopulation);
    }
}
