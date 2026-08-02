package com.acn.wildlifeextractor.infrastructure.schema;

import java.util.List;
import java.util.Set;

import com.acn.wildlifeextractor.domain.validation.SanitizedValidationError;
import com.acn.wildlifeextractor.domain.validation.SchemaValidationResult;
import com.acn.wildlifeextractor.domain.validation.StructuredOutputSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import org.springframework.stereotype.Component;

/**
 * {@link StructuredOutputSchemaValidator} backed by the networknt json-schema-validator. The
 * networknt types never escape this class, and error messages are reduced to safe structural
 * metadata so that no transcript-derived instance value is surfaced.
 */
@Component
public class NetworkntStructuredOutputSchemaValidator implements StructuredOutputSchemaValidator {

    private final CompiledSchemaCache schemaCache;

    public NetworkntStructuredOutputSchemaValidator(CompiledSchemaCache schemaCache) {
        this.schemaCache = schemaCache;
    }

    @Override
    public SchemaValidationResult validate(JsonNode output, JsonNode schema) {
        JsonSchema compiled = schemaCache.getOrCompile(schema);
        Set<ValidationMessage> messages = compiled.validate(output);
        if (messages.isEmpty()) {
            return SchemaValidationResult.success();
        }
        List<SanitizedValidationError> errors = messages.stream()
                .map(this::toSanitized)
                .toList();
        return SchemaValidationResult.failure(errors);
    }

    private SanitizedValidationError toSanitized(ValidationMessage message) {
        String keyword = message.getType();
        String path = message.getInstanceLocation() == null ? "" : message.getInstanceLocation().toString();
        String code = message.getMessageKey() != null ? message.getMessageKey() : keyword;
        // Deliberately excludes ValidationMessage.getMessage(), which can embed instance values.
        String safeDescription = "Schema constraint '" + keyword + "' violated at '" + path + "'";
        return new SanitizedValidationError(code, path, keyword, safeDescription);
    }
}
