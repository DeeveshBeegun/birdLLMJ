package com.acn.wildlifeextractor.application.validation;

import com.acn.wildlifeextractor.application.error.ModelOutputMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

/**
 * Maps the validated {@code fields} node onto a typed extraction DTO using the strict Jackson
 * mapper (unknown properties are rejected). Any mapping failure is surfaced as a structurally
 * repairable {@link ModelOutputMappingException}.
 */
@Component
public class ExtractionDtoMapper {

    private final ObjectMapper objectMapper;

    public ExtractionDtoMapper(ObjectMapper extractionObjectMapper) {
        this.objectMapper = extractionObjectMapper;
    }

    public <T> T map(JsonNode fieldsNode, Class<T> type) {
        JsonNode source = fieldsNode == null || fieldsNode.isNull()
                ? objectMapper.createObjectNode() : fieldsNode;
        try {
            return objectMapper.treeToValue(source, type);
        } catch (Exception e) {
            throw new ModelOutputMappingException(
                    "Unable to map model output to " + type.getSimpleName(), e);
        }
    }

    public ObjectNode emptyFields() {
        return objectMapper.createObjectNode();
    }
}
