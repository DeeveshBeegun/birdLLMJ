package com.acn.wildlifeextractor.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import com.acn.wildlifeextractor.application.confirmation.StoredExtraction;
import com.acn.wildlifeextractor.application.extraction.ExtractionCommand;
import com.acn.wildlifeextractor.application.extraction.WildlifeExtractionService;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * End-to-end test against a real Ollama server. Disabled unless {@code OLLAMA_IT=true} so the
 * normal test lifecycle never requires Ollama. Run with a pulled model available:
 * <pre>OLLAMA_IT=true ./mvnw test -Dtest=OllamaIntegrationTest</pre>
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OLLAMA_IT", matches = "true")
class OllamaIntegrationTest {

    @Autowired
    private WildlifeExtractionService service;

    @Test
    void extractsASightingUsingRealOllama() {
        StoredExtraction result = service.extract(new ExtractionCommand(
                "it-1", "conv-it", WildlifeFormType.SIGHTING,
                "At ten thirty I saw an Echo Parakeet near the feeder.",
                OffsetDateTime.parse("2026-07-30T10:30:00+04:00"), "en",
                null, null, null, null, null, null));

        assertThat(result.decision()).isNotNull();
        assertThat(result.modelCallCount()).isGreaterThanOrEqualTo(1);
    }
}
