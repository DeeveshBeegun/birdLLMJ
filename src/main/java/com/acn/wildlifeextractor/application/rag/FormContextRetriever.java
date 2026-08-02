package com.acn.wildlifeextractor.application.rag;

/**
 * Retrieves optional supplementary context for a form. Implementations must never introduce
 * observation facts that are not present in the transcript.
 */
public interface FormContextRetriever {

    RetrievedFormContext retrieve(FormContextQuery query);
}
