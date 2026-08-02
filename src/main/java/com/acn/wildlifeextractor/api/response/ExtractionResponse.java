package com.acn.wildlifeextractor.api.response;

import java.util.List;
import java.util.Map;

import com.acn.wildlifeextractor.domain.extraction.ExtractionDecision;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;

/**
 * The safe extraction preview returned to callers. Raw model output is never included; the
 * validated fields are provided as a plain JSON structure.
 */
public record ExtractionResponse(
        String requestId,
        String conversationId,
        WildlifeFormType formType,
        String schemaVersion,
        String promptVersion,
        ExtractionDecision decision,
        Object validatedFields,
        List<String> missingRequiredFields,
        List<String> ambiguousFields,
        List<String> invalidFields,
        List<String> unresolvedReferences,
        List<String> warnings,
        Map<String, Object> safeModelMetadata,
        int modelCallCount,
        int correctionAttemptCount,
        int infrastructureRetryCount
) {
}
