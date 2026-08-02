package com.acn.wildlifeextractor.infrastructure.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.acn.wildlifeextractor.application.error.ExtractionCapacityExceededException;
import com.acn.wildlifeextractor.application.error.ModelTimeoutException;
import com.acn.wildlifeextractor.application.error.ModelUnavailableException;
import com.acn.wildlifeextractor.application.extraction.RawModelExtractionResponse;
import com.acn.wildlifeextractor.application.extraction.StructuredWildlifeExtractionModel;
import com.acn.wildlifeextractor.application.extraction.WildlifeModelExtractionRequest;
import com.acn.wildlifeextractor.configuration.ExtractionProperties;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;

class ResilientWildlifeExtractionModelTest {

    private final ResilienceConfiguration config = new ResilienceConfiguration();

    private ExtractionProperties props(int maxConcurrent, int maxPending, Duration queueWait,
                                       Duration modelTimeout, int retries) {
        return new ExtractionProperties(15000, 100000, 1, modelTimeout, queueWait, maxConcurrent,
                maxPending, 20, 20, true, false, false, false, false, retries);
    }

    private ResilientWildlifeExtractionModel build(StructuredWildlifeExtractionModel delegate,
                                                   ExtractionProperties properties) {
        CircuitBreaker cb = config.extractionCircuitBreaker();
        Bulkhead bulkhead = config.extractionBulkhead(properties);
        ExecutorService executor = config.extractionModelExecutor(properties);
        return new ResilientWildlifeExtractionModel(delegate, cb, bulkhead, executor, properties);
    }

    private WildlifeModelExtractionRequest request() {
        return WildlifeModelExtractionRequest.initial(WildlifeFormType.SIGHTING, "sighting-v1", "t",
                OffsetDateTime.parse("2026-07-30T10:30:00+04:00"), "en", "sys", "form", "{}");
    }

    private RawModelExtractionResponse raw() {
        return new RawModelExtractionResponse("{}", "ollama", "llama3.1:8b", Duration.ofMillis(1), Map.of());
    }

    @Test
    void timesOutSlowCalls() {
        ExtractionProperties properties = props(2, 2, Duration.ofSeconds(1), Duration.ofMillis(100), 0);
        StructuredWildlifeExtractionModel slow = req -> {
            sleep(500);
            return raw();
        };
        ResilientWildlifeExtractionModel resilient = build(slow, properties);

        assertThatThrownBy(() -> resilient.extract(request()))
                .isInstanceOf(ModelTimeoutException.class);
    }

    @Test
    void retriesSafeTransientFailureThenSucceeds() {
        ExtractionProperties properties = props(2, 2, Duration.ofSeconds(1), Duration.ofSeconds(2), 1);
        AtomicInteger calls = new AtomicInteger();
        StructuredWildlifeExtractionModel flaky = req -> {
            if (calls.getAndIncrement() == 0) {
                throw new ModelUnavailableException("transient");
            }
            return raw();
        };
        ResilientWildlifeExtractionModel resilient = build(flaky, properties);

        RawModelExtractionResponse response = resilient.extract(request());

        assertThat(response).isNotNull();
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void rejectsWhenCapacityExceeded() throws Exception {
        ExtractionProperties properties = props(1, 0, Duration.ofMillis(200), Duration.ofSeconds(5), 0);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        StructuredWildlifeExtractionModel blocking = req -> {
            entered.countDown();
            awaitLatch(release);
            return raw();
        };
        ResilientWildlifeExtractionModel resilient = build(blocking, properties);

        Thread holder = new Thread(() -> resilient.extract(request()));
        holder.start();
        entered.await();

        try {
            assertThatThrownBy(() -> resilient.extract(request()))
                    .isInstanceOf(ExtractionCapacityExceededException.class);
        } finally {
            release.countDown();
            holder.join();
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
