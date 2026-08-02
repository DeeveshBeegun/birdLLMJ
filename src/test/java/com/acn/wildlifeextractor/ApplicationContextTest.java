package com.acn.wildlifeextractor;

import static org.assertj.core.api.Assertions.assertThat;

import com.acn.wildlifeextractor.configuration.ExtractionProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies that the application context starts successfully without a running Ollama
 * server and that typed configuration binds with the expected defaults.
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationContextTest {

    @Autowired
    private ExtractionProperties extractionProperties;

    @Test
    void contextLoadsWithoutOllama() {
        assertThat(extractionProperties).isNotNull();
    }

    @Test
    void extractionPropertiesBindWithExpectedDefaults() {
        assertThat(extractionProperties.maximumTranscriptLength()).isEqualTo(15000);
        assertThat(extractionProperties.maximumModelResponseLength()).isEqualTo(100000);
        assertThat(extractionProperties.maximumCorrectionAttempts()).isEqualTo(1);
        assertThat(extractionProperties.maximumConcurrentModelCalls()).isEqualTo(4);
        assertThat(extractionProperties.maximumPendingModelCalls()).isEqualTo(50);
        assertThat(extractionProperties.evidenceCheckEnabled()).isTrue();
        assertThat(extractionProperties.legacyFieldsEnabled()).isFalse();
        assertThat(extractionProperties.strictVocabularyValidation()).isFalse();
        assertThat(extractionProperties.ragEnabled()).isFalse();
    }
}
