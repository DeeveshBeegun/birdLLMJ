package com.acn.wildlifeextractor.application.decision;

import static org.assertj.core.api.Assertions.assertThat;

import com.acn.wildlifeextractor.application.validation.ValidationFindings;
import com.acn.wildlifeextractor.domain.extraction.ExtractionDecision;
import org.junit.jupiter.api.Test;

class ExtractionDecisionServiceTest {

    private final ExtractionDecisionService service = new ExtractionDecisionService();

    @Test
    void acceptsWhenThereAreNoFindings() {
        assertThat(service.decide(new ValidationFindings()))
                .isEqualTo(ExtractionDecision.ACCEPT_AUTOMATICALLY);
    }

    @Test
    void requestsMoreInformationWhenRequiredFieldMissing() {
        ValidationFindings findings = new ValidationFindings();
        findings.addMissingRequiredField("species");
        assertThat(service.decide(findings)).isEqualTo(ExtractionDecision.REQUEST_MORE_INFORMATION);
    }

    @Test
    void manualReviewForAmbiguity() {
        ValidationFindings findings = new ValidationFindings();
        findings.addAmbiguousField("seenSex");
        assertThat(service.decide(findings)).isEqualTo(ExtractionDecision.MANUAL_REVIEW);
    }

    @Test
    void manualReviewForContradiction() {
        ValidationFindings findings = new ValidationFindings();
        findings.addContradiction("start after end");
        assertThat(service.decide(findings)).isEqualTo(ExtractionDecision.MANUAL_REVIEW);
    }

    @Test
    void manualReviewForUnresolvedReference() {
        ValidationFindings findings = new ValidationFindings();
        findings.addUnresolvedReference("species");
        assertThat(service.decide(findings)).isEqualTo(ExtractionDecision.MANUAL_REVIEW);
    }

    @Test
    void manualReviewTakesPrecedenceOverMissingRequired() {
        ValidationFindings findings = new ValidationFindings();
        findings.addMissingRequiredField("species");
        findings.addInvalidField("latitude");
        assertThat(service.decide(findings)).isEqualTo(ExtractionDecision.MANUAL_REVIEW);
    }
}
