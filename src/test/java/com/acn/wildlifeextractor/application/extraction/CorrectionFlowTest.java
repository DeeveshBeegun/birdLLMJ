package com.acn.wildlifeextractor.application.extraction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

import com.acn.wildlifeextractor.application.confirmation.StoredExtraction;
import com.acn.wildlifeextractor.domain.extraction.ExtractionDecision;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class CorrectionFlowTest {

    @MockitoBean
    private StructuredWildlifeExtractionModel model;

    @Autowired
    private WildlifeExtractionService service;

    private static final String VALID = "{\"schemaVersion\":\"sighting-v1\",\"formType\":\"SIGHTING\","
            + "\"extractionStatus\":\"COMPLETE\",\"fields\":{"
            + "\"species\":{\"spokenValue\":\"Echo Parakeet\",\"referenceType\":\"SPECIES\",\"lookupKey\":\"Echo Parakeet\","
            + "\"resolvedExternalId\":null,\"resolvedDisplayName\":null,\"resolutionStatus\":null,"
            + "\"status\":\"PRESENT\",\"evidence\":\"Echo Parakeet\",\"candidates\":[],\"warnings\":[]}}}";

    private static final String SCHEMA_INVALID = "{\"schemaVersion\":\"sighting-v1\",\"formType\":\"SIGHTING\","
            + "\"extractionStatus\":\"INCOMPLETE\",\"fields\":{"
            + "\"latitude\":{\"rawValue\":\"200\",\"normalizedValue\":200,\"status\":\"PRESENT\","
            + "\"evidence\":\"two hundred\",\"warnings\":[]}}}";

    private static final String EMPTY = "{\"schemaVersion\":\"sighting-v1\",\"formType\":\"SIGHTING\","
            + "\"extractionStatus\":\"INCOMPLETE\",\"fields\":{}}";

    private RawModelExtractionResponse raw(String content) {
        return new RawModelExtractionResponse(content, "ollama", "llama3.1:8b",
                Duration.ofMillis(5), Map.of("model", "llama3.1:8b"));
    }

    private ExtractionCommand command(String requestId) {
        return new ExtractionCommand(requestId, "conv", WildlifeFormType.SIGHTING,
                "I saw an Echo Parakeet", OffsetDateTime.parse("2026-07-30T10:30:00+04:00"), "en");
    }

    @Test
    void structuralFailureIsCorrectedOnSecondAttempt() {
        when(model.extract(any())).thenReturn(raw(SCHEMA_INVALID), raw(VALID));

        StoredExtraction result = service.extract(command("corr-ok"));

        assertThat(result.decision()).isEqualTo(ExtractionDecision.ACCEPT_AUTOMATICALLY);
        assertThat(result.modelCallCount()).isEqualTo(2);
        assertThat(result.correctionAttemptCount()).isEqualTo(1);
        verify(model, times(2)).extract(any());
    }

    @Test
    void correctionExhaustionYieldsRejectResult() {
        when(model.extract(any())).thenReturn(raw(SCHEMA_INVALID), raw(SCHEMA_INVALID));

        StoredExtraction result = service.extract(command("corr-exhausted"));

        assertThat(result.decision()).isEqualTo(ExtractionDecision.REJECT);
        assertThat(result.modelCallCount()).isEqualTo(2);
        assertThat(result.correctionAttemptCount()).isEqualTo(1);
    }

    @Test
    void missingDataIsNotCorrected() {
        when(model.extract(any())).thenReturn(raw(EMPTY));

        StoredExtraction result = service.extract(command("corr-missing"));

        assertThat(result.decision()).isEqualTo(ExtractionDecision.REQUEST_MORE_INFORMATION);
        assertThat(result.correctionAttemptCount()).isZero();
        verify(model, times(1)).extract(any());
    }
}
