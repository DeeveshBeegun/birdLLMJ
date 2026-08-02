package com.acn.wildlifeextractor.infrastructure.rag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.acn.wildlifeextractor.application.rag.FormContextQuery;
import com.acn.wildlifeextractor.application.rag.RetrievedFormContext;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import org.junit.jupiter.api.Test;

class NoOpFormContextRetrieverTest {

    private final NoOpFormContextRetriever retriever = new NoOpFormContextRetriever();

    @Test
    void alwaysReturnsEmptyFallbackContext() {
        FormContextQuery query = new FormContextQuery(
                WildlifeFormType.SIGHTING, "sighting-v1", List.of("parakeet"), 5);
        RetrievedFormContext context = retriever.retrieve(query);

        assertThat(context.snippets()).isEmpty();
        assertThat(context.usedFallback()).isTrue();
    }
}
