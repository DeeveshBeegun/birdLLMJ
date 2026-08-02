package com.acn.wildlifeextractor.infrastructure.rag;

import com.acn.wildlifeextractor.application.rag.FormContextQuery;
import com.acn.wildlifeextractor.application.rag.FormContextRetriever;
import com.acn.wildlifeextractor.application.rag.RetrievedFormContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default {@link FormContextRetriever} used when RAG is disabled (the default). Always returns an
 * empty result so the pipeline behaves deterministically without any vector store.
 */
@Component
@ConditionalOnProperty(prefix = "application.extraction", name = "rag-enabled", havingValue = "false", matchIfMissing = true)
public class NoOpFormContextRetriever implements FormContextRetriever {

    @Override
    public RetrievedFormContext retrieve(FormContextQuery query) {
        return RetrievedFormContext.empty();
    }
}
