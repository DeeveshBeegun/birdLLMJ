package com.acn.wildlifeextractor.domain.form;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A registered wildlife form: its type, the versioned schema and prompt it uses, the typed
 * extraction payload it maps to, and the resources that drive extraction.
 *
 * @param <T> the form-specific extraction fields type
 */
public interface WildlifeFormDefinition<T> {

    WildlifeFormType formType();

    String schemaVersion();

    String promptVersion();

    Class<T> extractionType();

    JsonNode jsonSchema();

    String systemPrompt();

    String formPrompt();
}
