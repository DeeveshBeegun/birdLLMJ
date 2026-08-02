package com.acn.wildlifeextractor.support;

import java.time.Duration;

import com.acn.wildlifeextractor.configuration.ExtractionProperties;

/**
 * Builds {@link ExtractionProperties} instances for tests without loading a Spring context.
 */
public final class TestProperties {

    private TestProperties() {
    }

    public static ExtractionProperties defaults() {
        return new ExtractionProperties(
                15000,
                100000,
                1,
                Duration.ofSeconds(45),
                Duration.ofSeconds(10),
                4,
                50,
                20,
                20,
                true,
                false,
                false,
                false,
                false,
                1);
    }

    public static ExtractionProperties withMaximumModelResponseLength(int maximumModelResponseLength) {
        ExtractionProperties d = defaults();
        return new ExtractionProperties(
                d.maximumTranscriptLength(),
                maximumModelResponseLength,
                d.maximumCorrectionAttempts(),
                d.modelTimeout(),
                d.queueWaitTimeout(),
                d.maximumConcurrentModelCalls(),
                d.maximumPendingModelCalls(),
                d.maximumEggsPerObservation(),
                d.maximumChicksPerObservation(),
                d.evidenceCheckEnabled(),
                d.persistRawTranscript(),
                d.legacyFieldsEnabled(),
                d.strictVocabularyValidation(),
                d.ragEnabled(),
                d.maximumInfrastructureRetries());
    }

    public static ExtractionProperties withLegacyFieldsEnabled(boolean enabled) {
        ExtractionProperties d = defaults();
        return new ExtractionProperties(
                d.maximumTranscriptLength(),
                d.maximumModelResponseLength(),
                d.maximumCorrectionAttempts(),
                d.modelTimeout(),
                d.queueWaitTimeout(),
                d.maximumConcurrentModelCalls(),
                d.maximumPendingModelCalls(),
                d.maximumEggsPerObservation(),
                d.maximumChicksPerObservation(),
                d.evidenceCheckEnabled(),
                d.persistRawTranscript(),
                enabled,
                d.strictVocabularyValidation(),
                d.ragEnabled(),
                d.maximumInfrastructureRetries());
    }
}
