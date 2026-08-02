package com.acn.wildlifeextractor.infrastructure.springai.ollama;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Builds a simplified, grammar-safe JSON Schema to pass as Ollama's native {@code format}.
 *
 * <p>Ollama's grammar engine cannot compile the strict Draft 2020-12 schemas (they use
 * {@code $ref}/{@code $defs}/{@code allOf}/{@code if-then}/patterns), so a small model produces
 * structurally wrong output (fields as an array, null status, object-shaped lists). This factory
 * derives a schema Ollama <em>can</em> compile — single types (plus simple type unions), enums,
 * object {@code properties}, and string-array lists — which forces the correct envelope shape:
 * {@code fields} is an object keyed by the form's field names, each value a field object, and the
 * finding lists are arrays of strings. The strict schema still validates the details downstream.</p>
 */
@Component
public class OllamaGrammarSchemaFactory {

    // Statuses the model may produce. MISSING is excluded (missing fields are omitted) and
    // UNRESOLVED_REFERENCE is set only by the application, so allowing only these avoids
    // model output that the strict schema would reject.
    private static final List<String> MODEL_STATUSES = List.of(
            "PRESENT", "AMBIGUOUS", "INVALID", "NOT_APPLICABLE");

    private final ObjectMapper objectMapper;
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    public OllamaGrammarSchemaFactory(ObjectMapper extractionObjectMapper) {
        this.objectMapper = extractionObjectMapper;
    }

    /** Returns the grammar-safe schema for the given strict schema JSON, cached per schema version. */
    public Object build(String strictSchemaJson) {
        JsonNode strict;
        try {
            strict = objectMapper.readTree(strictSchemaJson);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse strict schema for grammar derivation", e);
        }
        String cacheKey = strict.path("title").asText(strict.path("$id").asText("anonymous"));
        return cache.computeIfAbsent(cacheKey, key -> buildGrammar(strict));
    }

    private Map<String, Object> buildGrammar(JsonNode strict) {
        String version = constText(strict, "schemaVersion");
        String formType = constText(strict, "formType");

        JsonNode defs = strict.path("$defs");
        JsonNode fieldProps = strict.path("properties").path("fields").path("properties");
        Map<String, Object> fieldProperties = new LinkedHashMap<>();
        fieldProps.properties().forEach(entry -> {
            JsonNode def = entry.getValue();
            fieldProperties.put(entry.getKey(), shapeFor(def, defs));
        });

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("type", "object");
        fields.put("properties", fieldProperties);
        fields.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("schemaVersion", enumOf(version));
        properties.put("formType", enumOf(formType));
        properties.put("extractionStatus", enumOf("COMPLETE", "INCOMPLETE", "INVALID"));
        properties.put("fields", fields);
        properties.put("missingRequiredFields", stringArray());
        properties.put("ambiguousFields", stringArray());
        properties.put("invalidFields", stringArray());
        properties.put("unresolvedReferences", stringArray());
        properties.put("warnings", stringArray());

        Map<String, Object> grammar = new LinkedHashMap<>();
        grammar.put("type", "object");
        grammar.put("properties", properties);
        grammar.put("required", List.of("schemaVersion", "formType", "extractionStatus", "fields",
                "missingRequiredFields", "ambiguousFields", "invalidFields", "unresolvedReferences", "warnings"));
        grammar.put("additionalProperties", false);
        return grammar;
    }

    private Map<String, Object> fieldShape() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("rawValue", type("string"));
        properties.put("normalizedValue", multiType("string", "number", "boolean", "null"));
        properties.put("vocabularyName", type("string"));
        properties.put("spokenValue", type("string"));
        properties.put("referenceType", type("string"));
        properties.put("lookupKey", type("string"));
        properties.put("status", enumOf(MODEL_STATUSES.toArray(new String[0])));
        properties.put("evidence", type("string"));
        properties.put("warnings", stringArray());

        Map<String, Object> shape = new LinkedHashMap<>();
        shape.put("type", "object");
        shape.put("properties", properties);
        // Require evidence: the strict schema demands it for PRESENT fields, and requiring it for
        // every emitted field is the reliable way to make a small model supply it.
        shape.put("required", List.of("status", "evidence"));
        shape.put("additionalProperties", false);
        return shape;
    }

    /** Chooses the grammar shape for a strict field def: array, nested object, or plain field. */
    private Object shapeFor(JsonNode def, JsonNode defs) {
        if ("array".equals(def.path("type").asText()) || def.has("items")) {
            Map<String, Object> array = new LinkedHashMap<>();
            array.put("type", "array");
            array.put("items", nestedObjectShape(defs.path(refName(def.path("items")))));
            return array;
        }
        JsonNode target = defs.path(refName(def));
        boolean isFieldObject = target.path("properties").has("status"); // scalar/code/reference field def
        if (target.isObject() && !isFieldObject && target.path("properties").isObject()) {
            return nestedObjectShape(target); // container like physicalCondition
        }
        return fieldShape();
    }

    /**
     * A container object constrained to its real sub-field names (each a field object) with no
     * extra keys, so the model cannot add a top-level status/evidence to physical-condition or
     * egg/chick entries.
     */
    private Map<String, Object> nestedObjectShape(JsonNode containerDef) {
        Map<String, Object> properties = new LinkedHashMap<>();
        containerDef.path("properties").properties()
                .forEach(entry -> properties.put(entry.getKey(), fieldShape()));
        Map<String, Object> object = new LinkedHashMap<>();
        object.put("type", "object");
        object.put("properties", properties);
        object.put("additionalProperties", false);
        return object;
    }

    /** The {@code $defs} name a field def references, directly or through a range-override allOf. */
    private String refName(JsonNode def) {
        String ref = null;
        if (def.has("$ref")) {
            ref = def.get("$ref").asText();
        } else {
            for (JsonNode part : def.path("allOf")) {
                if (part.has("$ref")) {
                    ref = part.get("$ref").asText();
                    break;
                }
            }
        }
        return ref == null ? "" : ref.substring(ref.lastIndexOf('/') + 1);
    }

    private Map<String, Object> stringArray() {
        Map<String, Object> array = new LinkedHashMap<>();
        array.put("type", "array");
        array.put("items", type("string"));
        return array;
    }

    private Map<String, Object> type(String type) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("type", type);
        return node;
    }

    private Map<String, Object> multiType(String... types) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("type", new ArrayList<>(List.of(types)));
        return node;
    }

    private Map<String, Object> enumOf(String... values) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("type", "string");
        node.put("enum", new ArrayList<>(List.of(values)));
        return node;
    }

    private String constText(JsonNode strict, String property) {
        return strict.path("properties").path(property).path("const").asText("");
    }
}
