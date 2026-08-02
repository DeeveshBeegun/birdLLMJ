package com.acn.wildlifeextractor.application.validation;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Read-only convenience accessor over a form's {@code fields} node, returning normalized values
 * only when the field is {@code PRESENT}. Returns {@code null} for absent or non-present fields so
 * business rules never confuse "missing" with a real value.
 */
public final class FieldsAccessor {

    private final JsonNode fields;

    public FieldsAccessor(JsonNode fields) {
        this.fields = fields;
    }

    public JsonNode raw(String name) {
        return fields != null && fields.has(name) ? fields.get(name) : null;
    }

    public String status(String name) {
        JsonNode field = raw(name);
        if (field == null) {
            return null;
        }
        JsonNode status = field.get("status");
        return status != null && status.isTextual() ? status.asText() : null;
    }

    public boolean isPresent(String name) {
        return "PRESENT".equals(status(name));
    }

    private JsonNode normalized(String name) {
        JsonNode field = raw(name);
        if (field == null) {
            return null;
        }
        JsonNode value = field.get("normalizedValue");
        return value == null || value.isNull() ? null : value;
    }

    public String textValue(String name) {
        JsonNode value = normalized(name);
        return value != null && value.isValueNode() ? value.asText() : null;
    }

    public Boolean booleanValue(String name) {
        JsonNode value = normalized(name);
        return value != null && value.isBoolean() ? value.booleanValue() : null;
    }

    public BigDecimal decimalValue(String name) {
        JsonNode value = normalized(name);
        return value != null && value.isNumber() ? value.decimalValue() : null;
    }

    public Integer integerValue(String name) {
        JsonNode value = normalized(name);
        return value != null && value.isNumber() ? value.intValue() : null;
    }

    /** Returns the normalized values of a code-list field as text, or an empty node when absent. */
    public JsonNode codeListValues(String name) {
        JsonNode field = raw(name);
        if (field == null) {
            return null;
        }
        return field.get("normalizedValues");
    }
}
