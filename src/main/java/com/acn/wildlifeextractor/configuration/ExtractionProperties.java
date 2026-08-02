package com.acn.wildlifeextractor.configuration;

import java.time.Duration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Typed, immutable configuration for the extraction pipeline.
 *
 * <p>Bound from the {@code application.extraction} prefix. All limits are validated at
 * startup so that an invalid configuration fails fast rather than surfacing at request time.</p>
 */
@Validated
@ConfigurationProperties(prefix = "application.extraction")
public record ExtractionProperties(

        @Min(1)
        @DefaultValue("15000")
        int maximumTranscriptLength,

        @Min(1)
        @DefaultValue("100000")
        int maximumModelResponseLength,

        @Min(0)
        @DefaultValue("1")
        int maximumCorrectionAttempts,

        @NotNull
        @DefaultValue("45s")
        Duration modelTimeout,

        @NotNull
        @DefaultValue("10s")
        Duration queueWaitTimeout,

        @Min(1)
        @DefaultValue("4")
        int maximumConcurrentModelCalls,

        @Min(1)
        @DefaultValue("50")
        int maximumPendingModelCalls,

        @Min(1)
        @DefaultValue("20")
        int maximumEggsPerObservation,

        @Min(1)
        @DefaultValue("20")
        int maximumChicksPerObservation,

        @DefaultValue("true")
        boolean evidenceCheckEnabled,

        @DefaultValue("false")
        boolean persistRawTranscript,

        @DefaultValue("false")
        boolean legacyFieldsEnabled,

        @DefaultValue("false")
        boolean strictVocabularyValidation,

        @DefaultValue("false")
        boolean ragEnabled,

        @Min(0)
        @DefaultValue("1")
        int maximumInfrastructureRetries
) {
}
