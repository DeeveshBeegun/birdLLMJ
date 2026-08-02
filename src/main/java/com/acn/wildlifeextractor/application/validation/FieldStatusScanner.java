package com.acn.wildlifeextractor.application.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

/**
 * Walks a form's {@code fields} JSON tree and locates every extracted-field object (any object
 * carrying a textual {@code status}). Field objects are treated as leaves; container objects such
 * as {@code physicalCondition} and nested egg/chick entries are traversed so their child fields
 * are found with a dotted/indexed path.
 */
@Component
public class FieldStatusScanner {

    /** A located field object and its path within the {@code fields} tree. */
    public record FieldOccurrence(String path, JsonNode node) {

        public String status() {
            JsonNode s = node.get("status");
            return s != null && s.isTextual() ? s.asText() : null;
        }
    }

    /** The result of a scan: all field objects, plus the code and reference subsets. */
    public record ScanResult(List<FieldOccurrence> allFields,
                             List<FieldOccurrence> codeFields,
                             List<FieldOccurrence> referenceFields) {
    }

    public ScanResult scan(JsonNode fields) {
        List<FieldOccurrence> all = new ArrayList<>();
        List<FieldOccurrence> codes = new ArrayList<>();
        List<FieldOccurrence> references = new ArrayList<>();
        if (fields != null && !fields.isNull()) {
            walk("", fields, all, codes, references);
        }
        return new ScanResult(List.copyOf(all), List.copyOf(codes), List.copyOf(references));
    }

    /** Populates status-derived findings (ambiguous, invalid, unresolved references). */
    public void collectStatusFindings(ScanResult scan, ValidationFindings findings) {
        for (FieldOccurrence field : scan.allFields()) {
            String status = field.status();
            if (status == null) {
                continue;
            }
            switch (status) {
                case "AMBIGUOUS" -> findings.addAmbiguousField(field.path());
                case "INVALID" -> findings.addInvalidField(field.path());
                case "UNRESOLVED_REFERENCE" -> findings.addUnresolvedReference(field.path());
                default -> { /* PRESENT, MISSING, NOT_APPLICABLE contribute no finding here */ }
            }
        }
        for (FieldOccurrence reference : scan.referenceFields()) {
            JsonNode resolution = reference.node().get("resolutionStatus");
            if (resolution != null && resolution.isTextual()) {
                String value = resolution.asText();
                if ("NOT_FOUND".equals(value) || "AMBIGUOUS".equals(value)) {
                    findings.addUnresolvedReference(reference.path());
                }
            }
        }
    }

    private void walk(String path, JsonNode node,
                      List<FieldOccurrence> all, List<FieldOccurrence> codes, List<FieldOccurrence> references) {
        if (node.isObject()) {
            if (isFieldObject(node)) {
                FieldOccurrence occurrence = new FieldOccurrence(path, node);
                all.add(occurrence);
                if (node.has("vocabularyName")) {
                    codes.add(occurrence);
                }
                if (node.has("resolutionStatus") || node.has("referenceType")) {
                    references.add(occurrence);
                }
                return; // field objects are leaves
            }
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                walk(join(path, entry.getKey()), entry.getValue(), all, codes, references);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                walk(path + "[" + i + "]", node.get(i), all, codes, references);
            }
        }
    }

    private boolean isFieldObject(JsonNode node) {
        JsonNode status = node.get("status");
        return status != null && status.isTextual();
    }

    private String join(String path, String name) {
        return path.isEmpty() ? name : path + "." + name;
    }
}
