package com.acn.wildlifeextractor.infrastructure.springai.ollama;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.acn.wildlifeextractor.application.extraction.WildlifeModelExtractionRequest;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.acn.wildlifeextractor.support.TestProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * Verifies that the Ollama adapter never logs sensitive content: transcript text, coordinates,
 * bird/ring/nest identifiers, or the raw model response.
 */
class SecurityLoggingTest {

    private static final String SECRET_TRANSCRIPT =
            "SECRET_TRANSCRIPT_TOKEN latitude -20.123456 bird BIRD-SECRET-001 ring RED-BLUE nest NEST-SECRET";

    @Test
    void adapterDoesNotLogSensitiveContent() {
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
                List.of(new Generation(new AssistantMessage("{\"schemaVersion\":\"sighting-v1\"}"))),
                ChatResponseMetadata.builder().model("llama3.1:8b").build()));

        SpringAiOllamaStructuredWildlifeExtractionModel adapter =
                new SpringAiOllamaStructuredWildlifeExtractionModel(
                        chatModel, new OllamaExtractionPromptFactory(new ObjectMapper()), new ObjectMapper(), TestProperties.defaults(),
                        new com.acn.wildlifeextractor.configuration.OllamaTuningProperties(false, 8192, 2048, "30m"),
                        new OllamaGrammarSchemaFactory(new ObjectMapper()), "test-model");

        Logger logger = (Logger) LoggerFactory.getLogger(SpringAiOllamaStructuredWildlifeExtractionModel.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            adapter.extract(WildlifeModelExtractionRequest.initial(WildlifeFormType.SIGHTING, "sighting-v1",
                    SECRET_TRANSCRIPT, OffsetDateTime.parse("2026-07-30T10:30:00+04:00"), "en",
                    "system", "form", "{\"type\":\"object\"}"));
        } finally {
            logger.detachAppender(appender);
        }

        assertThat(appender.list).isNotEmpty();
        for (ILoggingEvent event : appender.list) {
            String message = event.getFormattedMessage();
            assertThat(message).doesNotContain("SECRET_TRANSCRIPT_TOKEN");
            assertThat(message).doesNotContain("-20.123456");
            assertThat(message).doesNotContain("BIRD-SECRET-001");
            assertThat(message).doesNotContain("RED-BLUE");
            assertThat(message).doesNotContain("NEST-SECRET");
        }
    }
}
