package com.acn.wildlifeextractor.infrastructure.jev;

import java.util.Map;

import com.acn.wildlifeextractor.configuration.JevProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Thin HTTP client for the TypeSafe AI JEV System One API.
 *
 * <p>Sends a single batch request containing all questions for one extraction and returns the
 * answer map. On any transport or API error, an empty map is returned so the caller can
 * proceed without corrections (fail-open).</p>
 */
@Component
public class JevApiClient {

    private static final Logger log = LoggerFactory.getLogger(JevApiClient.class);

    private final RestClient restClient;
    private final JevProperties properties;

    public JevApiClient(RestClient.Builder builder, JevProperties properties) {
        this.properties = properties;
        this.restClient = builder
                .baseUrl(properties.endpoint())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Submits a JEV batch request and returns the answer map keyed by question id.
     * Returns an empty map if JEV is unreachable or returns an error.
     *
     * @param state     the shared context (e.g. {@code {"transcript": "..."}})
     * @param questions map of question-id → question definition (type/instructions/criteria)
     */
    public Map<String, JevAnswer> ask(Map<String, Object> state, Map<String, Object> questions) {
        JevRequest body = new JevRequest(properties.model(), state, questions);
        try {
            JevResponse response = restClient.post()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .body(body)
                    .retrieve()
                    .body(JevResponse.class);
            if (response == null || response.answers() == null) {
                log.warn("JEV returned null response");
                return Map.of();
            }
            return response.answers();
        } catch (RestClientException e) {
            log.warn("JEV call failed, proceeding without corrections: {}", e.getMessage());
            return Map.of();
        }
    }

    // ── Request/response model ────────────────────────────────────────────────

    record JevRequest(String model, Map<String, Object> state, Map<String, Object> questions) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record JevResponse(String model, Map<String, JevAnswer> answers) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JevAnswer(
            String type,
            String choice,
            Map<String, Double> probabilities,
            Double confidence
    ) {
    }
}
