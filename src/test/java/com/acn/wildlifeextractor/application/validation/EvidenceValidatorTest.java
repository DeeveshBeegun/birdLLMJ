package com.acn.wildlifeextractor.application.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.acn.wildlifeextractor.application.validation.FieldStatusScanner.ScanResult;
import com.acn.wildlifeextractor.support.TestJson;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

class EvidenceValidatorTest {

    private final FieldStatusScanner scanner = new FieldStatusScanner();
    private final EvidenceValidator validator = new EvidenceValidator();

    private ScanResult scan(String fieldsJson) {
        try {
            JsonNode fields = TestJson.strictMapper().readTree(fieldsJson);
            return scanner.scan(fields);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void acceptsEvidenceThatAppearsInTranscriptCaseInsensitively() {
        String fields = "{\"comment\":{\"normalizedValue\":\"note\",\"status\":\"PRESENT\","
                + "\"evidence\":\"near the   Nest\",\"warnings\":[]}}";
        ValidationFindings findings = new ValidationFindings();
        validator.validate(scan(fields), "The bird was seen near the nest today", findings);

        assertThat(findings.warnings()).isEmpty();
        assertThat(findings.requiresManualReview()).isFalse();
    }

    @Test
    void flagsEvidenceMissingFromTranscriptForManualReview() {
        String fields = "{\"comment\":{\"normalizedValue\":\"note\",\"status\":\"PRESENT\","
                + "\"evidence\":\"a purple elephant\",\"warnings\":[]}}";
        ValidationFindings findings = new ValidationFindings();
        validator.validate(scan(fields), "The bird was seen near the nest today", findings);

        assertThat(findings.warnings()).isNotEmpty();
        assertThat(findings.requiresManualReview()).isTrue();
    }
}
