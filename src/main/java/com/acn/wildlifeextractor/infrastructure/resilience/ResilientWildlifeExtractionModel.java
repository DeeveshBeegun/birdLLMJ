package com.acn.wildlifeextractor.infrastructure.resilience;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.acn.wildlifeextractor.application.error.ExtractionCapacityExceededException;
import com.acn.wildlifeextractor.application.error.ExtractionException;
import com.acn.wildlifeextractor.application.error.ModelTimeoutException;
import com.acn.wildlifeextractor.application.error.ModelUnavailableException;
import com.acn.wildlifeextractor.application.extraction.RawModelExtractionResponse;
import com.acn.wildlifeextractor.application.extraction.StructuredWildlifeExtractionModel;
import com.acn.wildlifeextractor.application.extraction.WildlifeModelExtractionRequest;
import com.acn.wildlifeextractor.configuration.ExtractionProperties;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Resilience decorator around the Ollama model adapter. It bounds concurrent and pending calls
 * (rejecting with a capacity error beyond the limits), enforces a per-call execution timeout,
 * trips a circuit breaker on repeated transport failures, and retries only safe transient
 * failures. Structural output failures pass straight through to be handled by the pipeline.
 */
@Component
@Primary
public class ResilientWildlifeExtractionModel implements StructuredWildlifeExtractionModel {

    private final StructuredWildlifeExtractionModel delegate;
    private final CircuitBreaker circuitBreaker;
    private final Bulkhead bulkhead;
    private final ExecutorService executor;
    private final ExtractionProperties properties;
    private final Semaphore admission;

    public ResilientWildlifeExtractionModel(
            @Qualifier("springAiOllamaStructuredWildlifeExtractionModel") StructuredWildlifeExtractionModel delegate,
            CircuitBreaker extractionCircuitBreaker,
            Bulkhead extractionBulkhead,
            @Qualifier("extractionModelExecutor") ExecutorService extractionModelExecutor,
            ExtractionProperties properties) {
        this.delegate = delegate;
        this.circuitBreaker = extractionCircuitBreaker;
        this.bulkhead = extractionBulkhead;
        this.executor = extractionModelExecutor;
        this.properties = properties;
        this.admission = new Semaphore(
                properties.maximumConcurrentModelCalls() + properties.maximumPendingModelCalls());
    }

    @Override
    public RawModelExtractionResponse extract(WildlifeModelExtractionRequest request) {
        if (!admission.tryAcquire()) {
            throw new ExtractionCapacityExceededException("Pending model-call queue is full");
        }
        try {
            bulkhead.acquirePermission();
        } catch (BulkheadFullException e) {
            admission.release();
            throw new ExtractionCapacityExceededException("No model-call capacity within the queue wait timeout");
        }
        try {
            return callWithRetry(request);
        } finally {
            bulkhead.releasePermission();
            admission.release();
        }
    }

    private RawModelExtractionResponse callWithRetry(WildlifeModelExtractionRequest request) {
        int attempts = 1 + Math.max(0, properties.maximumInfrastructureRetries());
        ModelUnavailableException lastTransient = null;
        for (int attempt = 0; attempt < attempts; attempt++) {
            try {
                return circuitBreaker.executeCallable(() -> callWithTimeout(request));
            } catch (CallNotPermittedException e) {
                throw new ModelUnavailableException("Circuit breaker is open", e);
            } catch (ModelUnavailableException e) {
                lastTransient = e; // safe transient failure: retry
            } catch (ExtractionException e) {
                throw e; // timeout and structural failures are not retried
            } catch (Exception e) {
                throw new ModelUnavailableException("Unexpected model failure", e);
            }
        }
        throw lastTransient;
    }

    private RawModelExtractionResponse callWithTimeout(WildlifeModelExtractionRequest request) {
        Future<RawModelExtractionResponse> future = executor.submit(() -> delegate.extract(request));
        try {
            return future.get(properties.modelTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ModelTimeoutException("Model call exceeded the configured timeout", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ModelUnavailableException("Model call was interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ExtractionException extractionException) {
                throw extractionException; // preserve structural/model errors verbatim
            }
            throw new ModelUnavailableException("Model provider call failed", cause);
        }
    }
}
