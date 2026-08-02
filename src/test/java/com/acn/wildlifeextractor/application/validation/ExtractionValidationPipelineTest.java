package com.acn.wildlifeextractor.application.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acn.wildlifeextractor.application.error.MalformedModelOutputException;
import com.acn.wildlifeextractor.application.error.SchemaValidationException;
import com.acn.wildlifeextractor.domain.extraction.ExtractionDecision;
import com.acn.wildlifeextractor.domain.form.WildlifeFormDefinition;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.acn.wildlifeextractor.infrastructure.form.WildlifeFormDefinitionRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ExtractionValidationPipelineTest {

    @Autowired
    private ExtractionValidationPipeline pipeline;

    @Autowired
    private WildlifeFormDefinitionRegistry registry;

    private WildlifeFormDefinition<?> sighting() {
        return registry.getByFormType(WildlifeFormType.SIGHTING);
    }

    private static final String TRANSCRIPT = "I saw a ring-necked parakeet near the feeder at ten thirty.";

    private static String speciesPresentEnvelope(String status) {
        return """
                {"schemaVersion":"sighting-v1","formType":"SIGHTING","extractionStatus":"COMPLETE","fields":{
                  "species":{"spokenValue":"ring-necked parakeet","referenceType":"SPECIES","lookupKey":"ring-necked parakeet",
                    "resolvedExternalId":null,"resolvedDisplayName":null,"resolutionStatus":null,
                    "status":"%s","evidence":"ring-necked parakeet","candidates":[],"warnings":[]}
                }}""".formatted(status);
    }

    @Test
    void acceptsAValidCompleteSighting() {
        ValidatedExtraction result = pipeline.validate(sighting(), speciesPresentEnvelope("PRESENT"), TRANSCRIPT);
        assertThat(result.decision()).isEqualTo(ExtractionDecision.ACCEPT_AUTOMATICALLY);
        assertThat(result.schemaVersion()).isEqualTo("sighting-v1");
    }

    @Test
    void requestsMoreInformationWhenRequiredSpeciesMissing() {
        String envelope = "{\"schemaVersion\":\"sighting-v1\",\"formType\":\"SIGHTING\","
                + "\"extractionStatus\":\"INCOMPLETE\",\"fields\":{}}";
        ValidatedExtraction result = pipeline.validate(sighting(), envelope, TRANSCRIPT);
        assertThat(result.decision()).isEqualTo(ExtractionDecision.REQUEST_MORE_INFORMATION);
        assertThat(result.missingRequiredFields()).contains("species");
    }

    @Test
    void routesAmbiguityToManualReview() {
        String envelope = """
                {"schemaVersion":"sighting-v1","formType":"SIGHTING","extractionStatus":"INCOMPLETE","fields":{
                  "species":{"spokenValue":"parakeet","referenceType":"SPECIES","lookupKey":"parakeet",
                    "resolvedExternalId":null,"resolvedDisplayName":null,"resolutionStatus":null,
                    "status":"PRESENT","evidence":"ring-necked parakeet","candidates":[],"warnings":[]},
                  "seenSex":{"rawValue":"male or female","normalizedValue":null,"vocabularyName":"BirdSex",
                    "status":"AMBIGUOUS","evidence":null,"warnings":[]}
                }}""";
        ValidatedExtraction result = pipeline.validate(sighting(), envelope, TRANSCRIPT);
        assertThat(result.decision()).isEqualTo(ExtractionDecision.MANUAL_REVIEW);
        assertThat(result.ambiguousFields()).contains("seenSex");
    }

    @Test
    void malformedJsonThrows() {
        assertThatThrownBy(() -> pipeline.validate(sighting(), "not json at all", TRANSCRIPT))
                .isInstanceOf(MalformedModelOutputException.class);
    }

    @Test
    void schemaViolationThrows() {
        String envelope = """
                {"schemaVersion":"sighting-v1","formType":"SIGHTING","extractionStatus":"INCOMPLETE","fields":{
                  "latitude":{"rawValue":"200","normalizedValue":200,"status":"PRESENT","evidence":"two hundred","warnings":[]}
                }}""";
        assertThatThrownBy(() -> pipeline.validate(sighting(), envelope, TRANSCRIPT))
                .isInstanceOf(SchemaValidationException.class);
    }

    @Test
    void unknownPropertyThrowsSchemaViolation() {
        String envelope = "{\"schemaVersion\":\"sighting-v1\",\"formType\":\"SIGHTING\","
                + "\"extractionStatus\":\"INCOMPLETE\",\"fields\":{\"bogus\":{}}}";
        assertThatThrownBy(() -> pipeline.validate(sighting(), envelope, TRANSCRIPT))
                .isInstanceOf(SchemaValidationException.class);
    }
}
