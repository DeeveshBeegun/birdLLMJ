package com.acn.wildlifeextractor.infrastructure.vocabulary;

import java.util.List;
import java.util.Map;

import com.acn.wildlifeextractor.domain.vocabulary.VocabularyValidationMode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The on-disk YAML shape of a single vocabulary definition. Unknown properties are rejected
 * so that typos in vocabulary files fail fast at startup.
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public record VocabularyYamlDocument(
        String name,
        VocabularyValidationMode mode,
        Boolean required,
        List<String> allowedValues,
        Map<String, String> aliases
) {
}
