package com.acn.wildlifeextractor.domain.extraction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acn.wildlifeextractor.domain.field.ExtractionFieldStatus;
import com.acn.wildlifeextractor.support.TestJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.Test;

/**
 * Round-trip and strictness tests for the form extraction DTOs. Absent JSON properties map to
 * {@code null} components; unknown properties and system-managed identifiers are rejected.
 */
class FormDtoSerializationTest {

    private final ObjectMapper mapper = TestJson.strictMapper();

    private <T> void assertRoundTrips(String json, Class<T> type) throws Exception {
        T first = mapper.readValue(json, type);
        String reserialized = mapper.writeValueAsString(first);
        T second = mapper.readValue(reserialized, type);
        assertThat(second).isEqualTo(first);
    }

    @Test
    void sightingRoundTripsWithPresentAndMissingValuesAndPhysicalCondition() throws Exception {
        String json = """
                {
                  "latitude": {"rawValue":"-20.3","normalizedValue":-20.3,"status":"PRESENT","evidence":"latitude minus twenty point three","warnings":[]},
                  "longitude": {"rawValue":null,"normalizedValue":null,"status":"MISSING","evidence":null,"warnings":[]},
                  "seenSex": {"rawValue":"male","normalizedValue":"MALE","vocabularyName":"BirdSex","status":"PRESENT","evidence":"a male","warnings":[]},
                  "behaviour": {"rawValues":["preening"],"normalizedValues":["PREENING"],"vocabularyName":"Behaviour","status":"PRESENT","evidence":"preening","warnings":[]},
                  "species": {"spokenValue":"echo parakeet","referenceType":"SPECIES","lookupKey":"echo parakeet","resolvedExternalId":null,"resolvedDisplayName":null,"resolutionStatus":"NOT_REQUESTED","status":"PRESENT","evidence":"echo parakeet","candidates":[],"warnings":[]},
                  "physicalCondition": {
                    "hasInjury": {"rawValue":"yes","normalizedValue":true,"status":"PRESENT","evidence":"injured wing","warnings":[]},
                    "clinicalSigns": {"rawValues":["lethargy"],"normalizedValues":["LETHARGY"],"vocabularyName":"ClinicalSigns","status":"PRESENT","evidence":"lethargic","warnings":[]}
                  }
                }
                """;
        SightingExtractionFields fields = mapper.readValue(json, SightingExtractionFields.class);

        assertThat(fields.latitude().status()).isEqualTo(ExtractionFieldStatus.PRESENT);
        assertThat(fields.longitude().status()).isEqualTo(ExtractionFieldStatus.MISSING);
        assertThat(fields.seenSex().normalizedValue()).isEqualTo("MALE");
        assertThat(fields.behaviour().normalizedValues()).containsExactly("PREENING");
        assertThat(fields.species().referenceType()).isEqualTo("SPECIES");
        assertThat(fields.physicalCondition().hasInjury().normalizedValue()).isTrue();
        // Absent components default to null.
        assertThat(fields.comment()).isNull();

        assertRoundTrips(json, SightingExtractionFields.class);
    }

    @Test
    void feedingObservationPreservesLegacySpellingProperty() throws Exception {
        String json = """
                {
                  "feedingOutCome": {"rawValue":"successful","normalizedValue":"SUCCESS","vocabularyName":"FeedingOutcome","status":"PRESENT","evidence":"fed successfully","warnings":[]},
                  "foragingHeight": {"rawValue":"about five metres","normalizedValue":"about five metres","status":"PRESENT","evidence":"five metres up","warnings":[]}
                }
                """;
        FeedingObservationExtractionFields fields = mapper.readValue(json, FeedingObservationExtractionFields.class);
        assertThat(fields.feedingOutCome().normalizedValue()).isEqualTo("SUCCESS");
        assertThat(fields.foragingHeight().rawValue()).isEqualTo("about five metres");
        assertRoundTrips(json, FeedingObservationExtractionFields.class);
    }

    @Test
    void nestSiteCharacteristicsRoundTrips() throws Exception {
        String json = """
                {
                  "bearing": {"rawValue":"120","normalizedValue":120,"status":"PRESENT","evidence":"bearing 120 degrees","warnings":[]},
                  "bird1Id": {"rawValue":"AB12","normalizedValue":"AB12","status":"PRESENT","evidence":"bird one AB12","warnings":[]}
                }
                """;
        assertRoundTrips(json, NestSiteCharacteristicsExtractionFields.class);
    }

    @Test
    void competitorsAndPredatorsRoundTrips() throws Exception {
        String json = """
                {
                  "numOfCompetitor": {"rawValue":"3","normalizedValue":3,"status":"PRESENT","evidence":"three mynas","warnings":[]},
                  "hasCompetitorInfo": {"rawValue":"yes","normalizedValue":true,"status":"PRESENT","evidence":"competitors present","warnings":[]},
                  "impact": {"rawValues":["nest disturbance"],"normalizedValues":["NEST_DISTURBANCE"],"vocabularyName":"Impact","status":"PRESENT","evidence":"disturbed the nest","warnings":[]}
                }
                """;
        assertRoundTrips(json, CompetitorsAndPredatorsExtractionFields.class);
    }

    @Test
    void nestEggsChickRoundTripsWithNestedEggsAndChicks() throws Exception {
        String json = """
                {
                  "clutchNumber": {"rawValue":"first","normalizedValue":"1","vocabularyName":"ClutchNumber","status":"PRESENT","evidence":"first clutch","warnings":[]},
                  "eggDetails": [
                    {"individualNumber": {"rawValue":"egg one","normalizedValue":"1","vocabularyName":"IndividualNumber","status":"PRESENT","evidence":"egg one","warnings":[]},
                     "fertility": {"rawValue":"fertile","normalizedValue":"FERTILE","vocabularyName":"EggStatus","status":"PRESENT","evidence":"fertile","warnings":[]}}
                  ],
                  "chickDetails": [
                    {"chickId": {"rawValue":"C1","normalizedValue":"C1","status":"PRESENT","evidence":"chick one","warnings":[]},
                     "age": {"rawValue":"5","normalizedValue":5,"status":"PRESENT","evidence":"five days old","warnings":[]}}
                  ]
                }
                """;
        NestEggsChickExtractionFields fields = mapper.readValue(json, NestEggsChickExtractionFields.class);
        assertThat(fields.eggDetails()).hasSize(1);
        assertThat(fields.chickDetails()).hasSize(1);
        assertThat(fields.eggDetails().get(0).fertility().normalizedValue()).isEqualTo("FERTILE");
        assertThat(fields.chickDetails().get(0).age().normalizedValue()).isEqualTo(5);
        assertRoundTrips(json, NestEggsChickExtractionFields.class);
    }

    @Test
    void emptyNestEggsChickHasEmptyLists() throws Exception {
        NestEggsChickExtractionFields fields = mapper.readValue("{}", NestEggsChickExtractionFields.class);
        assertThat(fields.eggDetails()).isEmpty();
        assertThat(fields.chickDetails()).isEmpty();
    }

    @Test
    void ringingMorphsRoundTripsWithPhysicalCondition() throws Exception {
        String json = """
                {
                  "weight": {"rawValue":"180","normalizedValue":180,"status":"PRESENT","evidence":"180 grams","warnings":[]},
                  "observer": {"spokenValue":"Jane","referenceType":"USER","lookupKey":"jane","resolvedExternalId":null,"resolvedDisplayName":null,"resolutionStatus":"NOT_REQUESTED","status":"PRESENT","evidence":"ringer Jane","candidates":[],"warnings":[]},
                  "physicalCondition": {"condition": {"rawValue":"good","normalizedValue":"GOOD","vocabularyName":"Condition","status":"PRESENT","evidence":"good condition","warnings":[]}}
                }
                """;
        assertRoundTrips(json, RingingMorphsExtractionFields.class);
    }

    @Test
    void unknownTopLevelPropertyIsRejected() {
        String json = """
                { "unexpectedField": {"rawValue":"x"} }
                """;
        assertThatThrownBy(() -> mapper.readValue(json, SightingExtractionFields.class))
                .isInstanceOf(UnrecognizedPropertyException.class);
    }

    @Test
    void systemManagedIdentifierIsRejectedAsUnknownProperty() {
        String json = """
                { "id": 42, "createdAt": "2026-07-30T10:00:00Z" }
                """;
        assertThatThrownBy(() -> mapper.readValue(json, SightingExtractionFields.class))
                .isInstanceOf(UnrecognizedPropertyException.class);
    }
}
