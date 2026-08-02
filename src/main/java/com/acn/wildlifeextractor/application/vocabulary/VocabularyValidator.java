package com.acn.wildlifeextractor.application.vocabulary;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.acn.wildlifeextractor.application.validation.FieldStatusScanner.FieldOccurrence;
import com.acn.wildlifeextractor.application.validation.FieldStatusScanner.ScanResult;
import com.acn.wildlifeextractor.application.validation.ValidationFindings;
import com.acn.wildlifeextractor.domain.vocabulary.DomainVocabularyProvider;
import com.acn.wildlifeextractor.domain.vocabulary.VocabularyDefinition;
import com.acn.wildlifeextractor.domain.vocabulary.VocabularyValidationMode;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

/**
 * Validates code-field values against their declared vocabulary.
 *
 * <ul>
 *   <li>{@code CLOSED}: only listed values or deterministic aliases are accepted; anything else is
 *       an invalid field.</li>
 *   <li>{@code OPEN} with a non-empty approved list: unlisted values produce a warning (and, in
 *       strict mode, a manual-review reason).</li>
 *   <li>{@code OPEN} with an empty approved list is treated as not-yet-constrained and accepted
 *       silently, avoiding warning noise while allowed values are still being curated.</li>
 * </ul>
 */
@Component
public class VocabularyValidator {

    private final DomainVocabularyProvider vocabularyProvider;

    public VocabularyValidator(DomainVocabularyProvider vocabularyProvider) {
        this.vocabularyProvider = vocabularyProvider;
    }

    public void validate(ScanResult scan, ValidationFindings findings, boolean strict) {
        for (FieldOccurrence code : scan.codeFields()) {
            validateCode(code, findings, strict);
        }
    }

    private void validateCode(FieldOccurrence code, ValidationFindings findings, boolean strict) {
        JsonNode node = code.node();
        if (!"PRESENT".equals(code.status())) {
            return;
        }
        JsonNode normalized = node.get("normalizedValue");
        if (normalized == null || normalized.isNull() || normalized.asText().isBlank()) {
            return;
        }
        String vocabularyName = text(node.get("vocabularyName"));
        if (vocabularyName == null || vocabularyName.isBlank()) {
            findings.addWarning("Missing vocabulary name at " + code.path());
            return;
        }
        Optional<VocabularyDefinition> definition = vocabularyProvider.find(vocabularyName);
        if (definition.isEmpty()) {
            findings.addWarning("Unknown vocabulary '" + vocabularyName + "' at " + code.path());
            return;
        }
        VocabularyDefinition vocabulary = definition.get();
        if (isApproved(vocabulary, normalized.asText())) {
            return;
        }
        if (vocabulary.validationMode() == VocabularyValidationMode.CLOSED) {
            findings.addInvalidField(code.path());
        } else if (!vocabulary.allowedValues().isEmpty()) {
            findings.addWarning("Unapproved '" + vocabularyName + "' value at " + code.path());
            if (strict) {
                findings.addManualReviewReason("Unapproved open-vocabulary value at " + code.path());
            }
        }
    }

    /** True when the value matches an allowed value or a deterministic alias, case-insensitively. */
    private boolean isApproved(VocabularyDefinition vocabulary, String value) {
        String candidate = normalize(value);
        for (String allowed : vocabulary.allowedValues()) {
            if (normalize(allowed).equals(candidate)) {
                return true;
            }
        }
        for (Map.Entry<String, String> alias : vocabulary.aliases().entrySet()) {
            if (normalize(alias.getKey()).equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private String text(JsonNode node) {
        return node != null && node.isTextual() ? node.asText() : null;
    }
}
