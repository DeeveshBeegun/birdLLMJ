package com.acn.wildlifeextractor.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.Map;

import com.acn.wildlifeextractor.application.error.ModelTimeoutException;
import com.acn.wildlifeextractor.application.error.ModelUnavailableException;
import com.acn.wildlifeextractor.application.extraction.RawModelExtractionResponse;
import com.acn.wildlifeextractor.application.extraction.StructuredWildlifeExtractionModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class WildlifeExtractionControllerTest {

    @MockitoBean
    private StructuredWildlifeExtractionModel model;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private MockMvc mvc() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        }
        return mockMvc;
    }

    private void modelReturns(String content) {
        when(model.extract(any())).thenReturn(new RawModelExtractionResponse(
                content, "ollama", "llama3.1:8b", Duration.ofMillis(5), Map.of("model", "llama3.1:8b")));
    }

    private static String request(String requestId, String transcript) {
        return """
                {"requestId":"%s","conversationId":"conv-1","formType":"SIGHTING",
                 "transcript":"%s","transcriptTimestamp":"2026-07-30T10:30:00+04:00","language":"en"}
                """.formatted(requestId, transcript);
    }

    private static String speciesEnvelope(String lookupKey, String status) {
        return ("{\\\"schemaVersion\\\":\\\"sighting-v1\\\",\\\"formType\\\":\\\"SIGHTING\\\","
                + "\\\"extractionStatus\\\":\\\"COMPLETE\\\",\\\"fields\\\":{"
                + "\\\"species\\\":{\\\"spokenValue\\\":\\\"" + lookupKey + "\\\",\\\"referenceType\\\":\\\"SPECIES\\\","
                + "\\\"lookupKey\\\":\\\"" + lookupKey + "\\\",\\\"resolvedExternalId\\\":null,\\\"resolvedDisplayName\\\":null,"
                + "\\\"resolutionStatus\\\":null,\\\"status\\\":\\\"" + status + "\\\",\\\"evidence\\\":\\\"" + lookupKey + "\\\","
                + "\\\"candidates\\\":[],\\\"warnings\\\":[]}}}").replace("\\\"", "\"");
    }

    private String stub(String requestId, String transcript, String envelope) {
        modelReturns(envelope);
        return request(requestId, transcript);
    }

    @Test
    void successfulExtractionReturnsAcceptDecision() throws Exception {
        String body = stub("req-1", "I saw an Echo Parakeet", speciesEnvelope("Echo Parakeet", "PRESENT"));
        mvc().perform(post("/api/v1/wildlife-extractions").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("ACCEPT_AUTOMATICALLY"))
                .andExpect(jsonPath("$.validatedFields.species.resolvedExternalId").value("SP-ECHO"))
                .andExpect(jsonPath("$.modelCallCount").value(1));
    }

    @Test
    void missingInformationReturnsRequestMoreInformation() throws Exception {
        modelReturns("{\"schemaVersion\":\"sighting-v1\",\"formType\":\"SIGHTING\","
                + "\"extractionStatus\":\"INCOMPLETE\",\"fields\":{}}");
        mvc().perform(post("/api/v1/wildlife-extractions").contentType(MediaType.APPLICATION_JSON)
                        .content(request("req-mi", "nothing useful")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("REQUEST_MORE_INFORMATION"));
    }

    @Test
    void ambiguityReturnsManualReview() throws Exception {
        String envelope = ("{\\\"schemaVersion\\\":\\\"sighting-v1\\\",\\\"formType\\\":\\\"SIGHTING\\\","
                + "\\\"extractionStatus\\\":\\\"INCOMPLETE\\\",\\\"fields\\\":{"
                + "\\\"species\\\":{\\\"spokenValue\\\":\\\"Echo Parakeet\\\",\\\"referenceType\\\":\\\"SPECIES\\\","
                + "\\\"lookupKey\\\":\\\"Echo Parakeet\\\",\\\"resolvedExternalId\\\":null,\\\"resolvedDisplayName\\\":null,"
                + "\\\"resolutionStatus\\\":null,\\\"status\\\":\\\"PRESENT\\\",\\\"evidence\\\":\\\"Echo Parakeet\\\","
                + "\\\"candidates\\\":[],\\\"warnings\\\":[]},"
                + "\\\"seenSex\\\":{\\\"rawValue\\\":\\\"maybe male\\\",\\\"normalizedValue\\\":null,"
                + "\\\"vocabularyName\\\":\\\"BirdSex\\\",\\\"status\\\":\\\"AMBIGUOUS\\\",\\\"evidence\\\":null,\\\"warnings\\\":[]}"
                + "}}").replace("\\\"", "\"");
        modelReturns(envelope);
        mvc().perform(post("/api/v1/wildlife-extractions").contentType(MediaType.APPLICATION_JSON)
                        .content(request("req-mr", "I saw an Echo Parakeet maybe male")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("MANUAL_REVIEW"));
    }

    @Test
    void blankTranscriptIsRejectedByValidation() throws Exception {
        mvc().perform(post("/api/v1/wildlife-extractions").contentType(MediaType.APPLICATION_JSON)
                        .content(request("req-blank", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_EXTRACTION_REQUEST"));
    }

    @Test
    void unsupportedFormValueIsRejected() throws Exception {
        String body = "{\"requestId\":\"r\",\"conversationId\":\"c\",\"formType\":\"NOT_A_FORM\","
                + "\"transcript\":\"x\",\"transcriptTimestamp\":\"2026-07-30T10:30:00+04:00\"}";
        mvc().perform(post("/api/v1/wildlife-extractions").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modelTimeoutReturnsGatewayTimeout() throws Exception {
        when(model.extract(any())).thenThrow(new ModelTimeoutException("timeout"));
        mvc().perform(post("/api/v1/wildlife-extractions").contentType(MediaType.APPLICATION_JSON)
                        .content(request("req-timeout", "hello")))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.errorCode").value("MODEL_TIMEOUT"));
    }

    @Test
    void modelUnavailableReturnsServiceUnavailable() throws Exception {
        when(model.extract(any())).thenThrow(new ModelUnavailableException("down"));
        mvc().perform(post("/api/v1/wildlife-extractions").contentType(MediaType.APPLICATION_JSON)
                        .content(request("req-down", "hello")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("MODEL_UNAVAILABLE"));
    }

    @Test
    void malformedModelJsonBecomesRejectResult() throws Exception {
        modelReturns("this is not json");
        mvc().perform(post("/api/v1/wildlife-extractions").contentType(MediaType.APPLICATION_JSON)
                        .content(request("req-malformed", "hello")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("REJECT"));
    }

    @Test
    void schemaInvalidOutputBecomesRejectResult() throws Exception {
        String envelope = ("{\\\"schemaVersion\\\":\\\"sighting-v1\\\",\\\"formType\\\":\\\"SIGHTING\\\","
                + "\\\"extractionStatus\\\":\\\"INCOMPLETE\\\",\\\"fields\\\":{"
                + "\\\"latitude\\\":{\\\"rawValue\\\":\\\"200\\\",\\\"normalizedValue\\\":200,\\\"status\\\":\\\"PRESENT\\\","
                + "\\\"evidence\\\":\\\"two hundred\\\",\\\"warnings\\\":[]}}}").replace("\\\"", "\"");
        modelReturns(envelope);
        mvc().perform(post("/api/v1/wildlife-extractions").contentType(MediaType.APPLICATION_JSON)
                        .content(request("req-schema", "latitude two hundred")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("REJECT"));
    }

    @Test
    void duplicateRequestReturnsCachedResultWithoutCallingModelTwice() throws Exception {
        modelReturns(speciesEnvelope("Echo Parakeet", "PRESENT"));
        String body = request("req-dup", "I saw an Echo Parakeet");
        mvc().perform(post("/api/v1/wildlife-extractions").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mvc().perform(post("/api/v1/wildlife-extractions").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        verify(model, times(1)).extract(any());
    }

    @Test
    void sameRequestIdDifferentContentReturnsConflict() throws Exception {
        modelReturns(speciesEnvelope("Echo Parakeet", "PRESENT"));
        mvc().perform(post("/api/v1/wildlife-extractions").contentType(MediaType.APPLICATION_JSON)
                        .content(request("req-conflict", "I saw an Echo Parakeet")))
                .andExpect(status().isOk());
        mvc().perform(post("/api/v1/wildlife-extractions").contentType(MediaType.APPLICATION_JSON)
                        .content(request("req-conflict", "a completely different transcript")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("IDEMPOTENCY_CONFLICT"));
    }

    @Test
    void confirmationSucceedsForAcceptedExtraction() throws Exception {
        modelReturns(speciesEnvelope("Echo Parakeet", "PRESENT"));
        mvc().perform(post("/api/v1/wildlife-extractions").contentType(MediaType.APPLICATION_JSON)
                .content(request("req-confirm", "I saw an Echo Parakeet")));
        mvc().perform(post("/api/v1/wildlife-extractions/req-confirm/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmed").value(true));
    }

    @Test
    void duplicateConfirmationIsIdempotent() throws Exception {
        modelReturns(speciesEnvelope("Echo Parakeet", "PRESENT"));
        mvc().perform(post("/api/v1/wildlife-extractions").contentType(MediaType.APPLICATION_JSON)
                .content(request("req-confirm2", "I saw an Echo Parakeet")));
        mvc().perform(post("/api/v1/wildlife-extractions/req-confirm2/confirm")).andExpect(status().isOk());
        mvc().perform(post("/api/v1/wildlife-extractions/req-confirm2/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmed").value(true));
    }

    @Test
    void confirmationOfUnknownRequestReturnsNotFound() throws Exception {
        mvc().perform(post("/api/v1/wildlife-extractions/does-not-exist/confirm"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("EXTRACTION_NOT_FOUND"));
    }
}
