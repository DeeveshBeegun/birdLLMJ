package com.acn.wildlifeextractor.infrastructure.schema;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.springframework.stereotype.Component;

/**
 * Compiles and caches JSON Schema Draft 2020-12 documents. Compilation is comparatively
 * expensive, so each schema is compiled once (keyed by its {@code $id}) and reused for every
 * subsequent validation.
 */
@Component
public class CompiledSchemaCache {

    private final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    private final ConcurrentMap<String, JsonSchema> cache = new ConcurrentHashMap<>();

    /**
     * Returns the compiled schema for the given schema document, compiling and caching it on
     * first use. A structurally invalid schema surfaces as an exception from the underlying
     * factory.
     */
    public JsonSchema getOrCompile(JsonNode schemaNode) {
        return cache.computeIfAbsent(keyFor(schemaNode), key -> factory.getSchema(schemaNode));
    }

    private String keyFor(JsonNode schemaNode) {
        JsonNode id = schemaNode.get("$id");
        if (id != null && id.isTextual() && !id.asText().isBlank()) {
            return id.asText();
        }
        // Stable within a single run for anonymous schemas used in tests.
        return "anonymous:" + System.identityHashCode(schemaNode);
    }
}
