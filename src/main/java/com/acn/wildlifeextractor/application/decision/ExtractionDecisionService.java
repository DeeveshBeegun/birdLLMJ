package com.acn.wildlifeextractor.application.decision;

import com.acn.wildlifeextractor.application.validation.ValidationFindings;
import com.acn.wildlifeextractor.domain.extraction.ExtractionDecision;
import org.springframework.stereotype.Component;

/**
 * Computes the deterministic acceptance decision from the accumulated findings.
 *
 * <p>The model never influences this beyond the field values it extracted. Structurally invalid or
 * unsafe output that survives correction is rejected upstream (the orchestrator sets
 * {@link ExtractionDecision#REJECT} directly); this service classifies successfully validated
 * output only.</p>
 */
@Component
public class ExtractionDecisionService {

    public ExtractionDecision decide(ValidationFindings findings) {
        if (findings.requiresManualReview()
                || findings.hasAmbiguousFields()
                || findings.hasInvalidFields()
                || findings.hasUnresolvedReferences()) {
            return ExtractionDecision.MANUAL_REVIEW;
        }
        if (findings.hasMissingRequiredFields()) {
            return ExtractionDecision.REQUEST_MORE_INFORMATION;
        }
        return ExtractionDecision.ACCEPT_AUTOMATICALLY;
    }
}
