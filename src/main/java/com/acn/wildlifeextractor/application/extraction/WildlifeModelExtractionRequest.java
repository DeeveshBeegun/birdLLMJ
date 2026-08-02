package com.acn.wildlifeextractor.application.extraction;

import java.time.OffsetDateTime;

import com.acn.wildlifeextractor.domain.form.WildlifeFormType;

/**
 * A provider-neutral request to extract a single form from a transcript. Carries everything the
 * model boundary needs and nothing provider-specific.
 */
public record WildlifeModelExtractionRequest(
        WildlifeFormType formType,
        String schemaVersion,
        String transcript,
        OffsetDateTime transcriptTimestamp,
        String language,
        String systemPrompt,
        String formPrompt,
        String jsonSchema,
        ModelInvocationPurpose purpose,
        String previousInvalidOutput,
        String structuralErrorSummary
) {

    /** Creates an initial-extraction request with no correction context. */
    public static WildlifeModelExtractionRequest initial(WildlifeFormType formType, String schemaVersion,
                                                         String transcript, OffsetDateTime transcriptTimestamp,
                                                         String language, String systemPrompt, String formPrompt,
                                                         String jsonSchema) {
        return new WildlifeModelExtractionRequest(formType, schemaVersion, transcript, transcriptTimestamp,
                language, systemPrompt, formPrompt, jsonSchema, ModelInvocationPurpose.INITIAL_EXTRACTION,
                null, null);
    }

    /** Creates a structural-correction request carrying the previous invalid output and sanitized errors. */
    public WildlifeModelExtractionRequest asCorrection(String previousInvalidOutput, String structuralErrorSummary) {
        return new WildlifeModelExtractionRequest(formType, schemaVersion, transcript, transcriptTimestamp,
                language, systemPrompt, formPrompt, jsonSchema, ModelInvocationPurpose.STRUCTURAL_CORRECTION,
                previousInvalidOutput, structuralErrorSummary);
    }
}
