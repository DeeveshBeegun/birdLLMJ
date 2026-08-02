package com.acn.wildlifeextractor.application.vocabulary;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.acn.wildlifeextractor.application.validation.FieldStatusScanner;
import com.acn.wildlifeextractor.application.validation.FieldStatusScanner.ScanResult;
import com.acn.wildlifeextractor.application.validation.ValidationFindings;
import com.acn.wildlifeextractor.domain.vocabulary.DomainVocabularyProvider;
import com.acn.wildlifeextractor.domain.vocabulary.VocabularyDefinition;
import com.acn.wildlifeextractor.domain.vocabulary.VocabularyValidationMode;
import com.acn.wildlifeextractor.support.TestJson;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

class VocabularyValidatorTest {

    private final FieldStatusScanner scanner = new FieldStatusScanner();

    private final DomainVocabularyProvider provider = new DomainVocabularyProvider() {
        private final Map<String, VocabularyDefinition> map = Map.of(
                "OpenEmpty", new VocabularyDefinition("OpenEmpty", VocabularyValidationMode.OPEN, Set.of(), Map.of(), false),
                "OpenList", new VocabularyDefinition("OpenList", VocabularyValidationMode.OPEN, Set.of("RED", "BLUE"), Map.of(), false),
                "ClosedSex", new VocabularyDefinition("ClosedSex", VocabularyValidationMode.CLOSED, Set.of("MALE", "FEMALE"), Map.of("m", "MALE"), false));

        @Override
        public VocabularyDefinition getRequired(String name) {
            return find(name).orElseThrow(() -> new IllegalArgumentException(name));
        }

        @Override
        public Optional<VocabularyDefinition> find(String name) {
            return Optional.ofNullable(map.get(name));
        }
    };

    private final VocabularyValidator validator = new VocabularyValidator(provider);

    private ScanResult scan(String fieldsJson) {
        try {
            JsonNode fields = TestJson.strictMapper().readTree(fieldsJson);
            return scanner.scan(fields);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String code(String vocab, String value) {
        return "{\"normalizedValue\":\"" + value + "\",\"vocabularyName\":\"" + vocab
                + "\",\"status\":\"PRESENT\",\"evidence\":\"e\",\"warnings\":[]}";
    }

    @Test
    void closedVocabularyRejectsUnknownValueAndAcceptsExactAndAlias() {
        String fields = "{"
                + "\"exact\":" + code("ClosedSex", "MALE") + ","
                + "\"alias\":" + code("ClosedSex", "m") + ","
                + "\"bad\":" + code("ClosedSex", "PURPLE")
                + "}";
        ValidationFindings findings = new ValidationFindings();
        validator.validate(scan(fields), findings, false);

        assertThat(findings.invalidFields()).contains("bad");
        assertThat(findings.invalidFields()).doesNotContain("exact", "alias");
    }

    @Test
    void openVocabularyWithListWarnsOnUnapprovedButAcceptsApprovedCaseInsensitively() {
        String fields = "{"
                + "\"approved\":" + code("OpenList", "red") + ","
                + "\"unapproved\":" + code("OpenList", "GREEN")
                + "}";
        ValidationFindings findings = new ValidationFindings();
        validator.validate(scan(fields), findings, false);

        assertThat(findings.invalidFields()).isEmpty();
        assertThat(findings.warnings()).anyMatch(w -> w.contains("unapproved"));
    }

    @Test
    void openVocabularyWithEmptyListAcceptsSilently() {
        ValidationFindings findings = new ValidationFindings();
        validator.validate(scan("{\"any\":" + code("OpenEmpty", "whatever") + "}"), findings, false);

        assertThat(findings.invalidFields()).isEmpty();
        assertThat(findings.warnings()).isEmpty();
    }

    @Test
    void unknownVocabularyProducesWarning() {
        ValidationFindings findings = new ValidationFindings();
        validator.validate(scan("{\"x\":" + code("DoesNotExist", "VALUE") + "}"), findings, false);

        assertThat(findings.warnings()).anyMatch(w -> w.contains("Unknown vocabulary"));
    }

    @Test
    void strictModeEscalatesUnapprovedOpenValueToManualReview() {
        ValidationFindings findings = new ValidationFindings();
        validator.validate(scan("{\"unapproved\":" + code("OpenList", "GREEN") + "}"), findings, true);

        assertThat(findings.requiresManualReview()).isTrue();
    }
}
