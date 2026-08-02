package com.acn.wildlifeextractor.support;

import java.io.InputStream;

import com.acn.wildlifeextractor.domain.validation.SchemaValidationResult;
import com.acn.wildlifeextractor.infrastructure.schema.CompiledSchemaCache;
import com.acn.wildlifeextractor.infrastructure.schema.NetworkntStructuredOutputSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test helper for loading a form schema from the classpath and validating envelope JSON.
 */
public final class Schemas {

    private static final ObjectMapper MAPPER = TestJson.strictMapper();
    private static final NetworkntStructuredOutputSchemaValidator VALIDATOR =
            new NetworkntStructuredOutputSchemaValidator(new CompiledSchemaCache());

    private Schemas() {
    }

    public static JsonNode load(String schemaVersion) {
        try (InputStream in = Schemas.class.getClassLoader()
                .getResourceAsStream("ai/schemas/" + schemaVersion + ".schema.json")) {
            if (in == null) {
                throw new IllegalStateException("Schema not found: " + schemaVersion);
            }
            return MAPPER.readTree(in);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load schema " + schemaVersion, e);
        }
    }

    public static SchemaValidationResult validate(String schemaVersion, String envelopeJson) {
        try {
            JsonNode output = MAPPER.readTree(envelopeJson);
            return VALIDATOR.validate(output, load(schemaVersion));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse envelope JSON", e);
        }
    }

    /**
     * Wraps a {@code fields} JSON fragment in a minimal valid envelope for the given form.
     */
    public static String envelope(String schemaVersion, String formType, String fieldsJson) {
        return "{\"schemaVersion\":\"" + schemaVersion + "\",\"formType\":\"" + formType
                + "\",\"extractionStatus\":\"INCOMPLETE\",\"fields\":" + fieldsJson + "}";
    }
}
