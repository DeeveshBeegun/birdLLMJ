package com.acn.wildlifeextractor.application.validation;

import com.acn.wildlifeextractor.application.error.MalformedModelOutputException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Parses raw, untrusted model text into a Jackson {@link JsonNode}. Anything that is not a JSON
 * object is rejected as malformed.
 */
@Component
public class ModelOutputParser {

    private final ObjectMapper objectMapper;

    public ModelOutputParser(ObjectMapper extractionObjectMapper) {
        this.objectMapper = extractionObjectMapper;
    }

    public JsonNode parse(String content) {
        if (content == null || content.isBlank()) {
            throw new MalformedModelOutputException("Model output was blank");
        }
        try {
            JsonNode node = objectMapper.readTree(content);
            if (node == null || !node.isObject()) {
                throw new MalformedModelOutputException("Model output was not a JSON object");
            }
            return node;
        } catch (JsonProcessingException e) {
            throw new MalformedModelOutputException("Model output was not valid JSON", e);
        }
    }
}
