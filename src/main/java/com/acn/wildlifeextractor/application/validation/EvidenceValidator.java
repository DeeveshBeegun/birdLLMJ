package com.acn.wildlifeextractor.application.validation;

import java.util.Locale;

import com.acn.wildlifeextractor.application.validation.FieldStatusScanner.FieldOccurrence;
import com.acn.wildlifeextractor.application.validation.FieldStatusScanner.ScanResult;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

/**
 * Conservatively checks that the supporting evidence for every {@code PRESENT} field actually
 * appears in the transcript. Comparison is case-insensitive and whitespace-normalized. Evidence
 * that cannot be located suggests possible fabrication and routes the extraction to manual review
 * rather than failing it outright.
 */
@Component
public class EvidenceValidator {

    public void validate(ScanResult scan, String transcript, ValidationFindings findings) {
        String haystack = normalize(transcript);
        for (FieldOccurrence field : scan.allFields()) {
            if (!"PRESENT".equals(field.status())) {
                continue;
            }
            JsonNode evidenceNode = field.node().get("evidence");
            if (evidenceNode == null || evidenceNode.isNull()) {
                continue;
            }
            String evidence = normalize(evidenceNode.asText());
            if (evidence.isBlank()) {
                continue;
            }
            if (!haystack.contains(evidence)) {
                findings.addWarning("Evidence not found in transcript for " + field.path());
                findings.addManualReviewReason("Unverifiable evidence at " + field.path());
            }
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
