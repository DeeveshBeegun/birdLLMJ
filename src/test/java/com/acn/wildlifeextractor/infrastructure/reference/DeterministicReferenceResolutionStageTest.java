package com.acn.wildlifeextractor.infrastructure.reference;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.acn.wildlifeextractor.application.validation.FieldStatusScanner;
import com.acn.wildlifeextractor.application.validation.FieldStatusScanner.ScanResult;
import com.acn.wildlifeextractor.application.validation.ValidationFindings;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.acn.wildlifeextractor.domain.reference.ReferenceResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class DeterministicReferenceResolutionStageTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final FieldStatusScanner scanner = new FieldStatusScanner();
    private final ReferenceDataLoader loader = new ReferenceDataLoader();
    private final List<ReferenceResolver> resolvers = List.of(
            new InMemorySpeciesReferenceResolver(loader),
            new InMemoryTreeSpeciesReferenceResolver(loader));
    private final DeterministicReferenceResolutionStage stage =
            new DeterministicReferenceResolutionStage(resolvers, mapper);

    private JsonNode fields(String speciesLookup) {
        String json = """
                {"species":{"spokenValue":"%s","referenceType":"SPECIES","lookupKey":"%s",
                  "resolvedExternalId":null,"resolvedDisplayName":null,"resolutionStatus":null,
                  "status":"PRESENT","evidence":"e","candidates":[],"warnings":[]}}
                """.formatted(speciesLookup, speciesLookup);
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void populatesResolvedIdentifierForKnownSpecies() {
        JsonNode fields = fields("Echo Parakeet");
        ScanResult scan = scanner.scan(fields);
        ValidationFindings findings = new ValidationFindings();

        stage.resolve(WildlifeFormType.SIGHTING, fields, scan, findings);

        JsonNode species = fields.get("species");
        assertThat(species.get("resolutionStatus").asText()).isEqualTo("RESOLVED");
        assertThat(species.get("resolvedExternalId").asText()).isEqualTo("SP-ECHO");
        assertThat(findings.hasUnresolvedReferences()).isFalse();
    }

    @Test
    void marksUnknownSpeciesUnresolvedAndRecordsFinding() {
        JsonNode fields = fields("Dodo");
        ScanResult scan = scanner.scan(fields);
        ValidationFindings findings = new ValidationFindings();

        stage.resolve(WildlifeFormType.SIGHTING, fields, scan, findings);

        JsonNode species = fields.get("species");
        assertThat(species.get("resolutionStatus").asText()).isEqualTo("NOT_FOUND");
        assertThat(species.get("status").asText()).isEqualTo("UNRESOLVED_REFERENCE");
        assertThat(findings.unresolvedReferences()).contains("species");
    }

    @Test
    void writesCandidatesForAmbiguousFuzzyMatch() {
        JsonNode fields = fields("parakeet");
        ScanResult scan = scanner.scan(fields);
        ValidationFindings findings = new ValidationFindings();

        stage.resolve(WildlifeFormType.SIGHTING, fields, scan, findings);

        JsonNode species = fields.get("species");
        assertThat(species.get("resolutionStatus").asText()).isEqualTo("AMBIGUOUS");
        assertThat(species.get("candidates")).isNotEmpty();
        assertThat(findings.unresolvedReferences()).contains("species");
    }
}
