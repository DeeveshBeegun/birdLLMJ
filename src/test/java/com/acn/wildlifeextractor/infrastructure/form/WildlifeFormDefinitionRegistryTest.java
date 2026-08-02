package com.acn.wildlifeextractor.infrastructure.form;

import static org.assertj.core.api.Assertions.assertThat;

import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Startup-level test: proves all six form definitions are discovered and their schemas compiled
 * successfully during context initialization.
 */
@SpringBootTest
@ActiveProfiles("test")
class WildlifeFormDefinitionRegistryTest {

    @Autowired
    private WildlifeFormDefinitionRegistry registry;

    @Test
    void discoversAllSixForms() {
        assertThat(registry.size()).isEqualTo(6);
        for (WildlifeFormType type : WildlifeFormType.values()) {
            assertThat(registry.findByFormType(type)).as("definition for %s", type).isPresent();
        }
    }

    @Test
    void exposesSchemaAndPromptsForEachForm() {
        registry.all().forEach(definition -> {
            assertThat(definition.jsonSchema().isObject()).isTrue();
            assertThat(definition.systemPrompt()).contains("structured form data");
            assertThat(definition.formPrompt()).isNotBlank();
            assertThat(definition.schemaVersion()).isNotBlank();
            assertThat(definition.promptVersion()).isNotBlank();
        });
    }

    @Test
    void resolvesBySchemaVersion() {
        assertThat(registry.findBySchemaVersion("sighting-v1")).isPresent();
        assertThat(registry.findBySchemaVersion("does-not-exist")).isEmpty();
    }
}
