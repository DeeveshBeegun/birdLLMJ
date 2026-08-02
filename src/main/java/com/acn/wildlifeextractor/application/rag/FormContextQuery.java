package com.acn.wildlifeextractor.application.rag;

import java.util.List;

import com.acn.wildlifeextractor.domain.form.WildlifeFormType;

/**
 * A request for supplementary, non-observational context to help interpret a transcript (for
 * example the meaning of a species name). It must never supply observation facts.
 */
public record FormContextQuery(
        WildlifeFormType formType,
        String schemaVersion,
        List<String> terms,
        int maxResults
) {
    public FormContextQuery {
        terms = terms == null ? List.of() : List.copyOf(terms);
    }
}
