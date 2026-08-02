package com.acn.wildlifeextractor.infrastructure.form;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.acn.wildlifeextractor.domain.form.WildlifeFormDefinition;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

/**
 * Base form definition that loads its JSON Schema and prompts from the classpath by convention:
 * <ul>
 *   <li>schema: {@code ai/schemas/<schemaVersion>.schema.json}</li>
 *   <li>shared system prompt: {@code ai/prompts/system-prompt.txt}</li>
 *   <li>form prompt: {@code ai/prompts/<schemaVersion>.prompt.txt}</li>
 * </ul>
 * All resources are loaded and validated eagerly so that a missing or malformed schema or
 * prompt aborts application startup.
 */
public abstract class AbstractClasspathFormDefinition<T> implements WildlifeFormDefinition<T> {

    private static final String SYSTEM_PROMPT_RESOURCE = "ai/prompts/system-prompt.txt";

    private final WildlifeFormType formType;
    private final String schemaVersion;
    private final String promptVersion;
    private final Class<T> extractionType;
    private final JsonNode jsonSchema;
    private final String systemPrompt;
    private final String formPrompt;

    protected AbstractClasspathFormDefinition(WildlifeFormType formType,
                                              String schemaVersion,
                                              String promptVersion,
                                              Class<T> extractionType,
                                              ObjectMapper objectMapper) {
        this.formType = formType;
        this.schemaVersion = schemaVersion;
        this.promptVersion = promptVersion;
        this.extractionType = extractionType;
        this.jsonSchema = loadSchema(schemaVersion, objectMapper);
        this.systemPrompt = loadText(SYSTEM_PROMPT_RESOURCE);
        this.formPrompt = loadText("ai/prompts/" + schemaVersion + ".prompt.txt");
        validate();
    }

    private void validate() {
        if (jsonSchema == null || !jsonSchema.isObject()) {
            throw new IllegalStateException("Schema for " + schemaVersion + " is missing or not an object");
        }
        if (systemPrompt.isBlank()) {
            throw new IllegalStateException("Shared system prompt is blank");
        }
        if (formPrompt.isBlank()) {
            throw new IllegalStateException("Form prompt for " + schemaVersion + " is blank");
        }
    }

    private static JsonNode loadSchema(String schemaVersion, ObjectMapper objectMapper) {
        ClassPathResource resource = new ClassPathResource("ai/schemas/" + schemaVersion + ".schema.json");
        try (InputStream in = resource.getInputStream()) {
            return objectMapper.readTree(in);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load schema for " + schemaVersion, e);
        }
    }

    private static String loadText(String resourcePath) {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream in = resource.getInputStream()) {
            return StreamUtils.copyToString(in, StandardCharsets.UTF_8).strip();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load resource " + resourcePath, e);
        }
    }

    @Override
    public WildlifeFormType formType() {
        return formType;
    }

    @Override
    public String schemaVersion() {
        return schemaVersion;
    }

    @Override
    public String promptVersion() {
        return promptVersion;
    }

    @Override
    public Class<T> extractionType() {
        return extractionType;
    }

    @Override
    public JsonNode jsonSchema() {
        return jsonSchema;
    }

    @Override
    public String systemPrompt() {
        return systemPrompt;
    }

    @Override
    public String formPrompt() {
        return formPrompt;
    }
}
