package com.acn.wildlifeextractor.infrastructure.resilience;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.acn.wildlifeextractor.application.error.MalformedModelOutputException;
import com.acn.wildlifeextractor.application.error.ModelOutputTooLargeException;
import com.acn.wildlifeextractor.application.error.ModelTimeoutException;
import com.acn.wildlifeextractor.application.error.ModelUnavailableException;
import com.acn.wildlifeextractor.configuration.ExtractionProperties;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j components for the model boundary: a circuit breaker that trips on repeated
 * transport failures (never on structural output problems), a bulkhead that bounds concurrent
 * model calls with a bounded queue wait, and a fixed executor that backs the per-call execution
 * timeout. The executor is shut down gracefully with the context.
 */
@Configuration
public class ResilienceConfiguration {

    @Bean
    CircuitBreaker extractionCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50f)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(2)
                .recordExceptions(ModelUnavailableException.class, ModelTimeoutException.class)
                .ignoreExceptions(MalformedModelOutputException.class, ModelOutputTooLargeException.class)
                .build();
        return CircuitBreaker.of("wildlife-extraction-model", config);
    }

    @Bean
    Bulkhead extractionBulkhead(ExtractionProperties properties) {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(properties.maximumConcurrentModelCalls())
                .maxWaitDuration(properties.queueWaitTimeout())
                .build();
        return Bulkhead.of("wildlife-extraction-model", config);
    }

    @Bean(name = "extractionModelExecutor", destroyMethod = "shutdown")
    ExecutorService extractionModelExecutor(ExtractionProperties properties) {
        return Executors.newFixedThreadPool(properties.maximumConcurrentModelCalls());
    }
}
