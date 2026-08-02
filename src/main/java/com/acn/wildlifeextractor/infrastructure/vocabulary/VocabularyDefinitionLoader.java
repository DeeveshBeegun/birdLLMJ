package com.acn.wildlifeextractor.infrastructure.vocabulary;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.acn.wildlifeextractor.domain.vocabulary.VocabularyDefinition;
import com.acn.wildlifeextractor.support.TextNormalizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Loads and structurally validates vocabulary definitions from YAML resources. This class is
 * free of Spring bean semantics so it can be unit-tested against arbitrary resource patterns.
 *
 * <p>Structural rules enforced here (any violation aborts loading):</p>
 * <ul>
 *   <li>Each document must declare a non-blank name and a validation mode.</li>
 *   <li>Vocabulary names must be unique across all files.</li>
 *   <li>An alias must map to exactly one canonical value; two alias keys that normalize to the
 *       same form must not map to different canonical values.</li>
 *   <li>An alias key that normalizes to an allowed value must map to that same value.</li>
 *   <li>For a CLOSED vocabulary with a non-empty allowed set, every alias target must be an
 *       allowed value.</li>
 * </ul>
 */
public class VocabularyDefinitionLoader {

    private final ResourcePatternResolver resolver;
    private final ObjectMapper yamlMapper;

    public VocabularyDefinitionLoader() {
        this(new PathMatchingResourcePatternResolver());
    }

    public VocabularyDefinitionLoader(ResourcePatternResolver resolver) {
        this.resolver = resolver;
        this.yamlMapper = YAMLMapper.builder().build();
    }

    public Map<String, VocabularyDefinition> load(String locationPattern) {
        Resource[] resources;
        try {
            resources = resolver.getResources(locationPattern);
        } catch (IOException e) {
            throw new InvalidVocabularyException("Unable to enumerate vocabulary resources at " + locationPattern, e);
        }

        Map<String, VocabularyDefinition> byName = new LinkedHashMap<>();
        for (Resource resource : resources) {
            VocabularyDefinition definition = readAndValidate(resource);
            if (byName.containsKey(definition.name())) {
                throw new InvalidVocabularyException("Duplicate vocabulary name: " + definition.name());
            }
            byName.put(definition.name(), definition);
        }
        return Collections.unmodifiableMap(byName);
    }

    private VocabularyDefinition readAndValidate(Resource resource) {
        VocabularyYamlDocument document;
        try (InputStream in = resource.getInputStream()) {
            document = yamlMapper.readValue(in, VocabularyYamlDocument.class);
        } catch (IOException e) {
            throw new InvalidVocabularyException("Unable to parse vocabulary file: " + safeName(resource), e);
        }
        if (document == null) {
            throw new InvalidVocabularyException("Empty vocabulary file: " + safeName(resource));
        }
        if (TextNormalizer.isBlank(document.name())) {
            throw new InvalidVocabularyException("Vocabulary file " + safeName(resource) + " is missing a name");
        }
        if (document.mode() == null) {
            throw new InvalidVocabularyException("Vocabulary '" + document.name() + "' is missing a validation mode");
        }

        List<String> allowedValues = document.allowedValues() == null ? List.of() : document.allowedValues();
        Map<String, String> aliases = document.aliases() == null ? Map.of() : document.aliases();

        validateAllowedValues(document.name(), allowedValues);
        validateAliases(document.name(), allowedValues, aliases, document.mode().name());

        boolean required = Boolean.TRUE.equals(document.required());
        return new VocabularyDefinition(
                document.name(),
                document.mode(),
                new LinkedHashSet<>(allowedValues),
                aliases,
                required);
    }

    private void validateAllowedValues(String vocabularyName, List<String> allowedValues) {
        Map<String, String> seen = new HashMap<>();
        for (String value : allowedValues) {
            if (TextNormalizer.isBlank(value)) {
                throw new InvalidVocabularyException("Vocabulary '" + vocabularyName + "' has a blank allowed value");
            }
            String normalized = TextNormalizer.normalizeForMatch(value);
            String existing = seen.putIfAbsent(normalized, value);
            if (existing != null && !existing.equals(value)) {
                throw new InvalidVocabularyException("Vocabulary '" + vocabularyName
                        + "' has allowed values that collide when normalized: '" + existing + "' and '" + value + "'");
            }
        }
    }

    private void validateAliases(String vocabularyName, List<String> allowedValues,
                                 Map<String, String> aliases, String mode) {
        Map<String, String> allowedByNormalized = new HashMap<>();
        for (String value : allowedValues) {
            allowedByNormalized.put(TextNormalizer.normalizeForMatch(value), value);
        }
        boolean closedWithAllowed = "CLOSED".equals(mode) && !allowedValues.isEmpty();

        Map<String, String> canonicalByNormalizedKey = new HashMap<>();
        for (Map.Entry<String, String> alias : aliases.entrySet()) {
            String key = alias.getKey();
            String canonical = alias.getValue();
            if (TextNormalizer.isBlank(key)) {
                throw new InvalidVocabularyException("Vocabulary '" + vocabularyName + "' has a blank alias key");
            }
            if (TextNormalizer.isBlank(canonical)) {
                throw new InvalidVocabularyException("Vocabulary '" + vocabularyName
                        + "' has alias '" + key + "' mapping to a blank canonical value");
            }
            String normalizedKey = TextNormalizer.normalizeForMatch(key);

            String priorCanonical = canonicalByNormalizedKey.putIfAbsent(normalizedKey, canonical);
            if (priorCanonical != null && !priorCanonical.equals(canonical)) {
                throw new InvalidVocabularyException("Vocabulary '" + vocabularyName + "' has ambiguous alias '"
                        + key + "' mapping to both '" + priorCanonical + "' and '" + canonical + "'");
            }

            String allowedForKey = allowedByNormalized.get(normalizedKey);
            if (allowedForKey != null && !allowedForKey.equals(canonical)) {
                throw new InvalidVocabularyException("Vocabulary '" + vocabularyName + "' has alias '" + key
                        + "' that conflicts with allowed value '" + allowedForKey + "'");
            }
            if (closedWithAllowed && !allowedByNormalized.containsKey(TextNormalizer.normalizeForMatch(canonical))) {
                throw new InvalidVocabularyException("Vocabulary '" + vocabularyName + "' is CLOSED but alias '" + key
                        + "' targets non-allowed value '" + canonical + "'");
            }
        }
    }

    private String safeName(Resource resource) {
        String name = resource.getFilename();
        return name == null ? resource.getDescription() : name;
    }
}
