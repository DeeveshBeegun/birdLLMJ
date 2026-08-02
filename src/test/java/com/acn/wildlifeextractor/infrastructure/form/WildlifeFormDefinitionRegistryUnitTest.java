package com.acn.wildlifeextractor.infrastructure.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import com.acn.wildlifeextractor.domain.form.UnsupportedWildlifeFormException;
import com.acn.wildlifeextractor.domain.form.WildlifeFormDefinition;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.acn.wildlifeextractor.infrastructure.schema.CompiledSchemaCache;
import com.acn.wildlifeextractor.support.TestJson;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

class WildlifeFormDefinitionRegistryUnitTest {

    private final CompiledSchemaCache cache = new CompiledSchemaCache();

    private WildlifeFormDefinition<Object> stub(WildlifeFormType type, String schemaVersion) {
        JsonNode schema;
        try {
            schema = TestJson.strictMapper().readTree("{\"type\":\"object\"}");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return new WildlifeFormDefinition<>() {
            @Override
            public WildlifeFormType formType() {
                return type;
            }

            @Override
            public String schemaVersion() {
                return schemaVersion;
            }

            @Override
            public String promptVersion() {
                return "p1";
            }

            @Override
            public Class<Object> extractionType() {
                return Object.class;
            }

            @Override
            public JsonNode jsonSchema() {
                return schema;
            }

            @Override
            public String systemPrompt() {
                return "system";
            }

            @Override
            public String formPrompt() {
                return "form";
            }
        };
    }

    @Test
    void rejectsDuplicateFormType() {
        List<WildlifeFormDefinition<?>> defs = List.of(
                stub(WildlifeFormType.SIGHTING, "sighting-v1"),
                stub(WildlifeFormType.SIGHTING, "sighting-v2"));
        assertThatThrownBy(() -> new WildlifeFormDefinitionRegistry(defs, cache))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate form type");
    }

    @Test
    void rejectsDuplicateSchemaVersion() {
        List<WildlifeFormDefinition<?>> defs = List.of(
                stub(WildlifeFormType.SIGHTING, "shared-v1"),
                stub(WildlifeFormType.RINGING_MORPHS, "shared-v1"));
        assertThatThrownBy(() -> new WildlifeFormDefinitionRegistry(defs, cache))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate schema version");
    }

    @Test
    void throwsForUnsupportedFormType() {
        WildlifeFormDefinitionRegistry registry = new WildlifeFormDefinitionRegistry(
                List.of(stub(WildlifeFormType.SIGHTING, "sighting-v1")), cache);
        assertThat(registry.getByFormType(WildlifeFormType.SIGHTING)).isNotNull();
        assertThatThrownBy(() -> registry.getByFormType(WildlifeFormType.RINGING_MORPHS))
                .isInstanceOf(UnsupportedWildlifeFormException.class);
    }
}
