package com.acn.wildlifeextractor.configuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Ollama request-tuning options bound from {@code application.extraction.ollama}. These control
 * latency and structured-output behaviour without touching Spring AI's own {@code spring.ai.ollama}
 * startup properties.
 *
 * @param nativeSchemaFormat when true, send the full JSON Schema as the native {@code format}
 *                           (only works on servers/schemas Ollama's grammar engine can parse);
 *                           otherwise use generic JSON mode and enforce the schema downstream
 * @param numCtx             context window; large enough to hold the prompt plus output
 * @param numPredict         hard cap on generated tokens, bounding worst-case latency
 * @param keepAlive          how long Ollama keeps the model loaded between calls (avoids reloads)
 */
@Validated
@ConfigurationProperties(prefix = "application.extraction.ollama")
public record OllamaTuningProperties(

        @DefaultValue("false")
        boolean nativeSchemaFormat,

        @Min(512)
        @DefaultValue("8192")
        int numCtx,

        @Min(64)
        @DefaultValue("2048")
        int numPredict,

        @NotBlank
        @DefaultValue("30m")
        String keepAlive
) {
}
