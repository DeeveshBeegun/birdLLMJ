package com.acn.wildlifeextractor.infrastructure.reference;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.acn.wildlifeextractor.application.reference.ReferenceResolutionStage;
import com.acn.wildlifeextractor.application.validation.FieldStatusScanner.FieldOccurrence;
import com.acn.wildlifeextractor.application.validation.FieldStatusScanner.ScanResult;
import com.acn.wildlifeextractor.application.validation.ValidationFindings;
import com.acn.wildlifeextractor.domain.field.ReferenceCandidate;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.acn.wildlifeextractor.domain.reference.ReferenceResolution;
import com.acn.wildlifeextractor.domain.reference.ReferenceResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

/**
 * Deterministically resolves every spoken reference against the trusted in-memory resolvers,
 * writing the resolution back into the (already schema-validated) reference field objects and
 * recording any that remain unresolved. The model's own resolution fields are ignored and
 * overwritten; only this stage may populate them.
 */
@Component
public class DeterministicReferenceResolutionStage implements ReferenceResolutionStage {

    private static final int MAX_CANDIDATES = 5;

    private final Map<String, ReferenceResolver> resolversByType = new LinkedHashMap<>();
    private final ObjectMapper objectMapper;

    public DeterministicReferenceResolutionStage(List<ReferenceResolver> resolvers, ObjectMapper extractionObjectMapper) {
        this.objectMapper = extractionObjectMapper;
        for (ReferenceResolver resolver : resolvers) {
            resolversByType.putIfAbsent(resolver.referenceType(), resolver);
        }
    }

    @Override
    public void resolve(WildlifeFormType formType, JsonNode fields, ScanResult scan, ValidationFindings findings) {
        for (FieldOccurrence occurrence : scan.referenceFields()) {
            if (!(occurrence.node() instanceof ObjectNode node)) {
                continue;
            }
            resolveReference(occurrence.path(), node, findings);
        }
    }

    private void resolveReference(String path, ObjectNode node, ValidationFindings findings) {
        if (!"PRESENT".equals(text(node.get("status")))) {
            node.put("resolutionStatus", "NOT_REQUESTED");
            return;
        }
        String referenceType = text(node.get("referenceType"));
        String lookupKey = firstNonBlank(text(node.get("lookupKey")), text(node.get("spokenValue")));
        ReferenceResolver resolver = referenceType == null ? null : resolversByType.get(referenceType);
        if (resolver == null) {
            markUnresolved(path, node, ReferenceResolution.notFound(), findings);
            return;
        }
        ReferenceResolution resolution = resolver.resolve(lookupKey);
        switch (resolution.status()) {
            case RESOLVED -> {
                node.put("resolutionStatus", "RESOLVED");
                node.put("resolvedExternalId", resolution.resolvedExternalId());
                node.put("resolvedDisplayName", resolution.resolvedDisplayName());
            }
            case NOT_FOUND, AMBIGUOUS -> markUnresolved(path, node, resolution, findings);
            case NOT_REQUESTED -> node.put("resolutionStatus", "NOT_REQUESTED");
        }
    }

    private void markUnresolved(String path, ObjectNode node, ReferenceResolution resolution,
                                ValidationFindings findings) {
        node.put("resolutionStatus", resolution.status().name());
        node.put("status", "UNRESOLVED_REFERENCE");
        writeCandidates(node, resolution.candidates());
        findings.addUnresolvedReference(path);
    }

    private void writeCandidates(ObjectNode node, List<ReferenceCandidate> candidates) {
        ArrayNode array = node.putArray("candidates");
        candidates.stream().limit(MAX_CANDIDATES)
                .forEach(candidate -> array.add(objectMapper.valueToTree(candidate)));
    }

    private String text(JsonNode node) {
        return node != null && node.isTextual() ? node.asText() : null;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}
