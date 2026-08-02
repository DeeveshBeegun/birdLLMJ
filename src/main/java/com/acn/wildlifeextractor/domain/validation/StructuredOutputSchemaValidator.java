package com.acn.wildlifeextractor.domain.validation;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Validates parsed model output against a JSON Schema. Implementations hide the concrete
 * schema library and must not leak sensitive instance values in the returned errors.
 */
public interface StructuredOutputSchemaValidator {

    SchemaValidationResult validate(JsonNode output, JsonNode schema);
}
