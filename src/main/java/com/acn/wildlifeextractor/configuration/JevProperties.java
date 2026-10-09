package com.acn.wildlifeextractor.configuration;

import java.time.Duration;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration for the JEV (TypeSafe AI System One) post-extraction correction stage.
 *
 * <p>When enabled, every PRESENT vocabulary-controlled field in the LLM output is verified
 * against the transcript via a JEV {@code choice} question. If JEV returns a different value
 * with confidence above {@code confidence-threshold}, the LLM value is replaced before
 * vocabulary validation runs.</p>
 */
@Validated
@ConfigurationProperties(prefix = "application.jev")
public record JevProperties(

        @DefaultValue("false")
        boolean enabled,

        @DefaultValue("")
        String apiKey,

        @DefaultValue("https://api.typesafe.ai/v1/systemone")
        String endpoint,

        @DefaultValue("jev-latest")
        String model,

        @DecimalMin("0.0") @DecimalMax("1.0")
        @DefaultValue("0.85")
        double confidenceThreshold,

        @Min(1)
        @DefaultValue("50")
        int maxVocabularySize,

        @DefaultValue("10s")
        Duration timeout
) {
}
