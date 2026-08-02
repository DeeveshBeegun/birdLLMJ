package com.acn.wildlifeextractor.infrastructure.persistence;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import com.acn.wildlifeextractor.application.confirmation.ExtractionRepository;
import com.acn.wildlifeextractor.application.confirmation.StoredExtraction;
import org.springframework.stereotype.Repository;

/**
 * Thread-safe in-memory {@link ExtractionRepository}. {@code computeIfAbsent} runs the supplier at
 * most once per request id even under concurrent access, which prevents duplicate model calls for
 * concurrent duplicate requests.
 */
@Repository
public class InMemoryExtractionRepository implements ExtractionRepository {

    private final ConcurrentMap<String, StoredExtraction> store = new ConcurrentHashMap<>();

    @Override
    public Optional<StoredExtraction> findById(String requestId) {
        return Optional.ofNullable(store.get(requestId));
    }

    @Override
    public StoredExtraction computeIfAbsent(String requestId, Supplier<StoredExtraction> supplier) {
        return store.computeIfAbsent(requestId, key -> supplier.get());
    }

    @Override
    public StoredExtraction update(StoredExtraction extraction) {
        store.put(extraction.requestId(), extraction);
        return extraction;
    }
}
