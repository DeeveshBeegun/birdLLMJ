package com.acn.wildlifeextractor.application.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.acn.wildlifeextractor.support.TestJson;
import com.acn.wildlifeextractor.support.TestProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

class BusinessValidatorTest {

    private final BusinessValidator validator = new BusinessValidator(TestProperties.defaults());

    private ValidationFindings run(WildlifeFormType type, String fieldsJson) {
        try {
            JsonNode fields = TestJson.strictMapper().readTree(fieldsJson);
            ValidationFindings findings = new ValidationFindings();
            validator.validate(type, fields, findings);
            return findings;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String decimal(String name, String value) {
        return "\"" + name + "\":{\"normalizedValue\":" + value + ",\"status\":\"PRESENT\",\"evidence\":\"e\",\"warnings\":[]}";
    }

    private static String time(String name, String value) {
        return "\"" + name + "\":{\"normalizedValue\":\"" + value + "\",\"status\":\"PRESENT\",\"evidence\":\"e\",\"warnings\":[]}";
    }

    private static String code(String name, String value) {
        return "\"" + name + "\":{\"normalizedValue\":\"" + value + "\",\"vocabularyName\":\"V\",\"status\":\"PRESENT\",\"evidence\":\"e\",\"warnings\":[]}";
    }

    @Test
    void rejectsLatitudeOutOfRange() {
        assertThat(run(WildlifeFormType.SIGHTING, "{" + decimal("latitude", "200") + "}").invalidFields())
                .contains("latitude");
    }

    @Test
    void rejectsNegativeCount() {
        assertThat(run(WildlifeFormType.SIGHTING, "{" + decimal("suspectedPairNumber", "-1") + "}").invalidFields())
                .contains("suspectedPairNumber");
    }

    @Test
    void mortalityDetailsTriggerManualReview() {
        assertThat(run(WildlifeFormType.SIGHTING, "{" + code("causeOfDeath", "PREDATION") + "}").requiresManualReview())
                .isTrue();
    }

    @Test
    void startAfterEndTimeIsContradiction() {
        String fields = "{" + time("startTime", "10:00") + "," + time("endTime", "09:00") + "}";
        assertThat(run(WildlifeFormType.SIGHTING, fields).contradictions()).isNotEmpty();
    }

    @Test
    void bearingOutOfRangeIsInvalid() {
        assertThat(run(WildlifeFormType.NEST_SITE_CHARACTERISTICS, "{" + decimal("bearing", "400") + "}").invalidFields())
                .contains("bearing");
    }

    @Test
    void competitorDetailsWithoutInfoFlagIsContradiction() {
        String fields = "{"
                + "\"hasCompetitorInfo\":{\"normalizedValue\":false,\"status\":\"PRESENT\",\"evidence\":\"e\",\"warnings\":[]},"
                + "\"competitorSpecies\":{\"spokenValue\":\"myna\",\"referenceType\":\"COMPETITOR_SPECIES\",\"status\":\"PRESENT\",\"evidence\":\"myna\",\"warnings\":[]}"
                + "}";
        assertThat(run(WildlifeFormType.COMPETITORS_AND_PREDATORS, fields).contradictions()).isNotEmpty();
    }

    @Test
    void physicalConditionInconsistencyTriggersManualReview() {
        String fields = "{\"physicalCondition\":{"
                + "\"hasClinicalSigns\":{\"normalizedValue\":false,\"status\":\"PRESENT\",\"evidence\":\"e\",\"warnings\":[]},"
                + "\"clinicalSigns\":{\"normalizedValues\":[\"lethargy\"],\"vocabularyName\":\"ClinicalSigns\",\"status\":\"PRESENT\",\"evidence\":\"e\",\"warnings\":[]}"
                + "}}";
        assertThat(run(WildlifeFormType.SIGHTING, fields).requiresManualReview()).isTrue();
    }

    @Test
    void duplicateEggIndividualNumbersTriggerManualReview() {
        String egg = "{\"individualNumber\":{\"normalizedValue\":\"1\",\"vocabularyName\":\"IndividualNumber\",\"status\":\"PRESENT\",\"evidence\":\"e\",\"warnings\":[]}}";
        String fields = "{\"eggDetails\":[" + egg + "," + egg + "]}";
        assertThat(run(WildlifeFormType.NEST_EGGS_CHICK, fields).requiresManualReview()).isTrue();
    }

    @Test
    void hatchBeforeLayingIsContradiction() {
        String egg = "{\"individualNumber\":{\"normalizedValue\":\"1\",\"vocabularyName\":\"IndividualNumber\",\"status\":\"PRESENT\",\"evidence\":\"e\",\"warnings\":[]},"
                + "\"dateLaid\":{\"normalizedValue\":\"2026-05-10\",\"status\":\"PRESENT\",\"evidence\":\"e\",\"warnings\":[]}}";
        String chick = "{\"individualNumber\":{\"normalizedValue\":\"1\",\"vocabularyName\":\"IndividualNumber\",\"status\":\"PRESENT\",\"evidence\":\"e\",\"warnings\":[]},"
                + "\"hatchDate\":{\"normalizedValue\":\"2026-05-01\",\"status\":\"PRESENT\",\"evidence\":\"e\",\"warnings\":[]}}";
        String fields = "{\"eggDetails\":[" + egg + "],\"chickDetails\":[" + chick + "]}";
        assertThat(run(WildlifeFormType.NEST_EGGS_CHICK, fields).contradictions()).isNotEmpty();
    }
}
