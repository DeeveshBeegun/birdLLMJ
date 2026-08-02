package com.acn.wildlifeextractor.application.confirmation;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Thread-safe store of completed extractions, keyed by request id.
 */
public interface ExtractionRepository {

    Optional<StoredExtraction> findById(String requestId);

    /**
     * Atomically returns the existing extraction for the request id, or computes, stores and
     * returns a new one. Guarantees a request id is only computed once even under concurrent
     * duplicate requests.
     */
    StoredExtraction computeIfAbsent(String requestId, Supplier<StoredExtraction> supplier);

    /** Replaces an existing record (for example after confirmation). */
    StoredExtraction update(StoredExtraction extraction);
}
