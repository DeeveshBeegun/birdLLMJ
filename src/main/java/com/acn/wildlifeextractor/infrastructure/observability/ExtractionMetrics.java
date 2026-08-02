package com.acn.wildlifeextractor.infrastructure.observability;

import java.time.Duration;

import com.acn.wildlifeextractor.domain.extraction.ExtractionDecision;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Micrometer instrumentation for extraction. Only low-cardinality tags are used (form type,
 * decision, provider, model, failure category); request ids, transcripts, coordinates and
 * identifiers are never used as tags.
 */
@Component
public class ExtractionMetrics {

    private final MeterRegistry registry;

    public ExtractionMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordOutcome(WildlifeFormType formType, ExtractionDecision decision,
                              int modelCallCount, int correctionAttemptCount, Duration totalLatency) {
        String form = formType.name();
        registry.counter("wildlife.extraction.requests", "formType", form).increment();
        registry.counter("wildlife.extraction.decisions", "formType", form, "decision", decision.name()).increment();
        registry.counter("wildlife.extraction.model.calls", "formType", form).increment(modelCallCount);
        if (correctionAttemptCount > 0) {
            registry.counter("wildlife.extraction.correction.attempts", "formType", form)
                    .increment(correctionAttemptCount);
        }
        registry.timer("wildlife.extraction.total.latency", "formType", form).record(totalLatency);
    }

    public void recordModelLatency(String provider, String model, Duration latency) {
        registry.timer("wildlife.extraction.model.latency", "provider", provider, "model", model).record(latency);
    }

    public void recordFailure(String failureCategory) {
        registry.counter("wildlife.extraction.failures", "failureCategory", failureCategory).increment();
    }
}
