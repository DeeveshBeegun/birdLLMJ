package com.acn.wildlifeextractor.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * Structural coverage test for the evaluation fixtures. Computing accuracy metrics against them
 * requires a running Ollama server (the Ollama integration-test profile); this test only checks
 * that the fixtures are well-formed and cover all six forms and the key adversarial categories.
 */
class EvaluationFixturesTest {

    private JsonNode load() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("evaluation/fixtures.json")) {
            assertThat(in).as("fixtures.json present").isNotNull();
            return new ObjectMapper().readTree(in);
        }
    }

    @Test
    void fixturesCoverEveryFormType() throws Exception {
        JsonNode fixtures = load().get("fixtures");
        Set<String> forms = new HashSet<>();
        for (JsonNode fixture : fixtures) {
            assertThat(fixture.hasNonNull("id")).isTrue();
            assertThat(fixture.has("transcript")).isTrue();
            assertThat(fixture.get("categories").isArray()).isTrue();
            forms.add(fixture.get("formType").asText());
        }
        for (WildlifeFormType type : WildlifeFormType.values()) {
            assertThat(forms).as("fixture for %s", type).contains(type.name());
        }
    }

    @Test
    void fixturesIncludeAdversarialCategories() throws Exception {
        JsonNode fixtures = load().get("fixtures");
        Set<String> categories = new HashSet<>();
        fixtures.forEach(fixture -> fixture.get("categories").forEach(c -> categories.add(c.asText())));
        assertThat(categories).contains("prompt-injection", "empty-transcript", "contradictory-values",
                "nested-eggs", "invalid-coordinate");
    }
}
