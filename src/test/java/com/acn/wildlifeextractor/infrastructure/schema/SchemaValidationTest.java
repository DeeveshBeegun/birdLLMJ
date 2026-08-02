package com.acn.wildlifeextractor.infrastructure.schema;

import static org.assertj.core.api.Assertions.assertThat;

import com.acn.wildlifeextractor.domain.validation.SchemaValidationResult;
import com.acn.wildlifeextractor.support.Schemas;
import org.junit.jupiter.api.Test;

/**
 * Validates the strict JSON Schemas against representative valid and invalid envelopes.
 */
class SchemaValidationTest {

    private SchemaValidationResult validateSighting(String fields) {
        return Schemas.validate("sighting-v1", Schemas.envelope("sighting-v1", "SIGHTING", fields));
    }

    @Test
    void allFormSchemasAcceptAMinimalValidEnvelope() {
        record Form(String version, String type) {
        }
        Form[] forms = {
                new Form("sighting-v1", "SIGHTING"),
                new Form("feeding-observation-v1", "FEEDING_OBSERVATION"),
                new Form("nest-site-characteristics-v1", "NEST_SITE_CHARACTERISTICS"),
                new Form("competitors-and-predators-v1", "COMPETITORS_AND_PREDATORS"),
                new Form("nest-eggs-chick-v1", "NEST_EGGS_CHICK"),
                new Form("ringing-morphs-v1", "RINGING_MORPHS")
        };
        for (Form form : forms) {
            SchemaValidationResult result = Schemas.validate(form.version(),
                    Schemas.envelope(form.version(), form.type(), "{}"));
            assertThat(result.valid()).as("minimal envelope for %s", form.version()).isTrue();
        }
    }

    @Test
    void acceptsCompleteValidSighting() {
        String fields = """
                {
                  "latitude": {"rawValue":"-20.3","normalizedValue":-20.3,"status":"PRESENT","evidence":"latitude minus twenty","warnings":[]},
                  "observedTime": {"rawValue":"10:30","normalizedValue":"10:30","status":"PRESENT","evidence":"ten thirty","warnings":[]},
                  "seenSex": {"rawValue":"male","normalizedValue":"MALE","vocabularyName":"BirdSex","status":"PRESENT","evidence":"a male bird","warnings":[]},
                  "comment": {"rawValue":null,"normalizedValue":null,"status":"MISSING","evidence":null,"warnings":[]}
                }
                """;
        assertThat(validateSighting(fields).valid()).isTrue();
    }

    @Test
    void rejectsLatitudeOutOfRange() {
        String fields = """
                { "latitude": {"rawValue":"200","normalizedValue":200,"status":"PRESENT","evidence":"two hundred","warnings":[]} }
                """;
        SchemaValidationResult result = validateSighting(fields);
        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(e -> e.validationKeyword().equals("maximum"));
    }

    @Test
    void rejectsMalformedTimeFormat() {
        String fields = """
                { "observedTime": {"rawValue":"half ten","normalizedValue":"10:30:00","status":"PRESENT","evidence":"ten thirty","warnings":[]} }
                """;
        assertThat(validateSighting(fields).valid()).isFalse();
    }

    @Test
    void rejectsMalformedDateFormat() {
        String fields = """
                { "observedTime": {"rawValue":"today","normalizedValue":"2026/07/30","status":"PRESENT","evidence":"today","warnings":[]} }
                """;
        SchemaValidationResult result = Schemas.validate("feeding-observation-v1",
                Schemas.envelope("feeding-observation-v1", "FEEDING_OBSERVATION", fields));
        assertThat(result.valid()).isFalse();
    }

    @Test
    void rejectsNegativeCount() {
        String fields = """
                { "suspectedPairNumber": {"rawValue":"-1","normalizedValue":-1,"status":"PRESENT","evidence":"pair","warnings":[]} }
                """;
        assertThat(validateSighting(fields).valid()).isFalse();
    }

    @Test
    void rejectsNegativeMeasurement() {
        String fields = """
                { "weight": {"rawValue":"-5","normalizedValue":-5,"status":"PRESENT","evidence":"weight","warnings":[]} }
                """;
        SchemaValidationResult result = Schemas.validate("ringing-morphs-v1",
                Schemas.envelope("ringing-morphs-v1", "RINGING_MORPHS", fields));
        assertThat(result.valid()).isFalse();
    }

    @Test
    void rejectsPresentFieldWithoutEvidence() {
        String fields = """
                { "comment": {"rawValue":"a note","normalizedValue":"a note","status":"PRESENT","evidence":null,"warnings":[]} }
                """;
        assertThat(validateSighting(fields).valid()).isFalse();
    }

    @Test
    void rejectsMissingFieldWithNormalizedValue() {
        String fields = """
                { "comment": {"rawValue":null,"normalizedValue":"ghost value","status":"MISSING","evidence":null,"warnings":[]} }
                """;
        assertThat(validateSighting(fields).valid()).isFalse();
    }

    @Test
    void rejectsUnknownPropertyInFields() {
        assertThat(validateSighting("{ \"bogusField\": {} }").valid()).isFalse();
    }

    @Test
    void rejectsGeneratedIdentifier() {
        assertThat(validateSighting("{ \"id\": 42 }").valid()).isFalse();
    }

    @Test
    void rejectsLegacyPropertyWhenDisabled() {
        // The default schema does not declare the legacy section, so it is rejected.
        String fields = """
                { "legacy": {"diameter": {"rawValue":"5","normalizedValue":5,"status":"PRESENT","evidence":"five","warnings":[]}} }
                """;
        SchemaValidationResult result = Schemas.validate("nest-site-characteristics-v1",
                Schemas.envelope("nest-site-characteristics-v1", "NEST_SITE_CHARACTERISTICS", fields));
        assertThat(result.valid()).isFalse();
    }

    @Test
    void rejectsOversizedEggList() {
        StringBuilder eggs = new StringBuilder("[");
        for (int i = 0; i < 21; i++) {
            if (i > 0) {
                eggs.append(",");
            }
            eggs.append("{\"comments\":{\"rawValue\":\"e\",\"normalizedValue\":\"e\",\"status\":\"PRESENT\",\"evidence\":\"egg\",\"warnings\":[]}}");
        }
        eggs.append("]");
        String fields = "{ \"eggDetails\": " + eggs + " }";
        SchemaValidationResult result = Schemas.validate("nest-eggs-chick-v1",
                Schemas.envelope("nest-eggs-chick-v1", "NEST_EGGS_CHICK", fields));
        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(e -> e.validationKeyword().equals("maxItems"));
    }

    @Test
    void rejectsInvalidNestedEggWithUnknownProperty() {
        String fields = """
                { "eggDetails": [ {"unexpected": {"rawValue":"x"}} ] }
                """;
        SchemaValidationResult result = Schemas.validate("nest-eggs-chick-v1",
                Schemas.envelope("nest-eggs-chick-v1", "NEST_EGGS_CHICK", fields));
        assertThat(result.valid()).isFalse();
    }

    @Test
    void rejectsInvalidNestedChickWithNegativeWeight() {
        String fields = """
                { "chickDetails": [ {"weight": {"rawValue":"-2","normalizedValue":-2,"status":"PRESENT","evidence":"weight","warnings":[]}} ] }
                """;
        SchemaValidationResult result = Schemas.validate("nest-eggs-chick-v1",
                Schemas.envelope("nest-eggs-chick-v1", "NEST_EGGS_CHICK", fields));
        assertThat(result.valid()).isFalse();
    }

    @Test
    void rejectsInvalidPhysicalConditionWithUnknownProperty() {
        String fields = """
                { "physicalCondition": {"bogusTrait": {"rawValue":"x"}} }
                """;
        assertThat(validateSighting(fields).valid()).isFalse();
    }

    @Test
    void rejectsWrongSchemaVersionConstant() {
        String envelope = Schemas.envelope("feeding-observation-v1", "SIGHTING", "{}");
        // formType SIGHTING does not match the feeding schema's const.
        assertThat(Schemas.validate("feeding-observation-v1", envelope).valid()).isFalse();
    }
}
