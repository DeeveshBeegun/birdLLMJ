package com.acn.wildlifeextractor.infrastructure.springai.ollama;

import java.util.ArrayList;
import java.util.List;

import com.acn.wildlifeextractor.application.extraction.ModelInvocationPurpose;
import com.acn.wildlifeextractor.application.extraction.WildlifeModelExtractionRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

/**
 * Builds the system and user messages for an extraction request.
 *
 * <p>The shared system prompt and the form-specific prompt form the system context. Because Ollama
 * cannot enforce these JSON Schemas natively (its grammar engine rejects {@code $ref}/{@code allOf}/
 * nullable unions), the user message also carries an explicit output contract derived from the
 * schema — the exact envelope keys, the field-object shape, the allowed field names, and a small
 * example — so the model produces conforming structure without a grammar. The transcript is placed
 * only in the user message, wrapped in delimiters, and is never concatenated into the system
 * message.</p>
 */
@Component
public class OllamaExtractionPromptFactory {

    private final ObjectMapper objectMapper;

    public OllamaExtractionPromptFactory(ObjectMapper extractionObjectMapper) {
        this.objectMapper = extractionObjectMapper;
    }

    public SystemMessage systemMessage(WildlifeModelExtractionRequest request) {
        String text = request.systemPrompt().strip() + "\n\n" + request.formPrompt().strip();
        return new SystemMessage(text);
    }

    public UserMessage userMessage(WildlifeModelExtractionRequest request) {
        if (request.purpose() == ModelInvocationPurpose.STRUCTURAL_CORRECTION
                && request.previousInvalidOutput() != null) {
            return new UserMessage(correctionText(request));
        }
        String timestamp = request.transcriptTimestamp() == null
                ? "unknown" : request.transcriptTimestamp().toString();
        String language = request.language() == null || request.language().isBlank()
                ? "unspecified" : request.language();
        String text = """
                Extract the selected wildlife form from the speech-to-text transcript.

                Form type:
                %s

                Transcript timestamp:
                %s

                Language:
                %s

                <transcript>
                %s
                </transcript>

                %s
                """.formatted(request.formType(), timestamp, language, request.transcript(),
                outputContract(request));
        return new UserMessage(text);
    }

    /**
     * Builds the explicit output contract from the form schema: exact top-level keys, the
     * field-object shape, which fields are codes or references, the allowed field names, and a
     * minimal example.
     */
    private String outputContract(WildlifeModelExtractionRequest request) {
        JsonNode schema = readSchema(request.jsonSchema());
        JsonNode fieldProps = schema.path("properties").path("fields").path("properties");
        String schemaVersion = constText(schema, "schemaVersion", request.schemaVersion());
        String formType = constText(schema, "formType", request.formType().name());

        List<String> allNames = new ArrayList<>();
        List<String> codeNames = new ArrayList<>();
        List<String> referenceNames = new ArrayList<>();
        fieldProps.properties().forEach(entry -> {
            allNames.add(entry.getKey());
            String kind = refKind(entry.getValue());
            if (kind.contains("extractedCode")) {
                codeNames.add(entry.getKey());
            } else if (kind.contains("extractedReference")) {
                referenceNames.add(entry.getKey());
            }
        });

        return """
                Output requirements (return ONLY this JSON object, no prose, no code fences):
                Top-level keys, exactly: "schemaVersion", "formType", "extractionStatus", "fields",
                "missingRequiredFields", "ambiguousFields", "invalidFields", "unresolvedReferences", "warnings".
                - "schemaVersion" must be "%s".
                - "formType" must be "%s".
                - "extractionStatus" is one of "COMPLETE", "INCOMPLETE", "INVALID".
                - The four *Fields/*References keys and "warnings" are JSON arrays (use [] when empty).
                - "fields" is an object whose keys are ONLY the allowed field names listed below.

                Each field value is an object:
                {"rawValue": <string|null>, "normalizedValue": <value|null>,
                 "status": "PRESENT|AMBIGUOUS|INVALID|NOT_APPLICABLE",
                 "evidence": <exact transcript phrase for PRESENT>, "warnings": []}
                Include a field ONLY when the transcript states something about it, and OMIT every
                field that is not mentioned (do not emit MISSING fields). This keeps "fields" small
                and the response fast.
                Code fields additionally include "vocabularyName". Code fields: %s
                Reference fields use {"spokenValue","referenceType","lookupKey","status","evidence","warnings"}
                and never include resolved ids. Reference fields: %s

                Allowed field names (use only these, and only when mentioned): %s

                Example:
                {"schemaVersion":"%s","formType":"%s","extractionStatus":"INCOMPLETE","fields":{"observedTime":{"rawValue":"ten thirty","normalizedValue":"10:30","status":"PRESENT","evidence":"at ten thirty","warnings":[]}},"missingRequiredFields":[],"ambiguousFields":[],"invalidFields":[],"unresolvedReferences":[],"warnings":[]}
                """.formatted(schemaVersion, formType,
                codeNames.isEmpty() ? "(none)" : String.join(", ", codeNames),
                referenceNames.isEmpty() ? "(none)" : String.join(", ", referenceNames),
                String.join(", ", allNames),
                schemaVersion, formType);
    }

    private JsonNode readSchema(String schemaJson) {
        try {
            return objectMapper.readTree(schemaJson);
        } catch (Exception e) {
            return objectMapper.createObjectNode();
        }
    }

    private String constText(JsonNode schema, String property, String fallback) {
        JsonNode value = schema.path("properties").path(property).path("const");
        return value.isTextual() ? value.asText() : fallback;
    }

    /** Returns the $ref target of a field schema, resolving a range-override allOf wrapper. */
    private String refKind(JsonNode fieldSchema) {
        if (fieldSchema.has("$ref")) {
            return fieldSchema.get("$ref").asText();
        }
        if (fieldSchema.has("allOf")) {
            for (JsonNode part : fieldSchema.get("allOf")) {
                if (part.has("$ref")) {
                    return part.get("$ref").asText();
                }
            }
        }
        return "";
    }

    private String correctionText(WildlifeModelExtractionRequest request) {
        return """
                The previous output did not satisfy the required JSON structure.

                Correct only the listed structural errors.

                Do not invent information.

                Use only facts explicitly present in the original transcript.

                Do not replace missing information with false, zero, empty strings,
                empty arrays, midnight, or placeholder values.

                Do not create identifiers or resolved references.

                Return only the corrected JSON object.

                Do not return Markdown or explanations.

                <transcript>
                %s
                </transcript>

                <previous-output>
                %s
                </previous-output>

                <structural-errors>
                %s
                </structural-errors>

                <json-schema>
                %s
                </json-schema>
                """.formatted(request.transcript(), request.previousInvalidOutput(),
                request.structuralErrorSummary() == null ? "" : request.structuralErrorSummary(),
                request.jsonSchema());
    }
}
