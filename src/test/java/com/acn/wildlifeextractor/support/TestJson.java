package com.acn.wildlifeextractor.support;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Builds an {@link ObjectMapper} configured identically to the application mapper, for use in
 * fast unit tests that do not need a Spring context.
 */
public final class TestJson {

    private TestJson() {
    }

    public static ObjectMapper strictMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}
