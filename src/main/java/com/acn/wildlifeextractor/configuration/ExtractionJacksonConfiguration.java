package com.acn.wildlifeextractor.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a dedicated Jackson 2 ({@code com.fasterxml.jackson}) {@link ObjectMapper} for the
 * extraction pipeline.
 *
 * <p>Spring Boot 4.1 auto-configures Jackson 3 ({@code tools.jackson}) for the web layer, but the
 * JSON Schema validator (networknt 1.5.x) and the schema/DTO handling in this application operate
 * on Jackson 2 {@code JsonNode}s. This mapper is strict: it rejects unknown properties so the
 * model can never introduce fields outside the declared schema.</p>
 */
@Configuration
public class ExtractionJacksonConfiguration {

    @Bean
    ObjectMapper extractionObjectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}
