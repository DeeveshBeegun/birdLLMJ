package com.acn.wildlifeextractor.application.rag;

import java.util.List;

/**
 * Retrieved contextual snippets. {@code usedFallback} indicates the retriever was unavailable or
 * disabled and a safe empty result was returned instead.
 */
public record RetrievedFormContext(
        List<String> snippets,
        boolean usedFallback
) {
    public RetrievedFormContext {
        snippets = snippets == null ? List.of() : List.copyOf(snippets);
    }

    public static RetrievedFormContext empty() {
        return new RetrievedFormContext(List.of(), true);
    }
}
