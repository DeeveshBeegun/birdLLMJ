package com.acn.wildlifeextractor.infrastructure.springai.ollama;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.acn.wildlifeextractor.application.error.MalformedModelOutputException;
import com.acn.wildlifeextractor.application.error.ModelOutputTooLargeException;
import com.acn.wildlifeextractor.application.extraction.RawModelExtractionResponse;
import com.acn.wildlifeextractor.application.extraction.WildlifeModelExtractionRequest;
import com.acn.wildlifeextractor.configuration.OllamaTuningProperties;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.acn.wildlifeextractor.support.TestProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;

class SpringAiOllamaStructuredWildlifeExtractionModelTest {

    private static final String SCHEMA = "{\"type\":\"object\",\"properties\":{\"fields\":{\"type\":\"object\"}}}";

    private final ObjectMapper mapper = new ObjectMapper();
    private final OllamaExtractionPromptFactory promptFactory = new OllamaExtractionPromptFactory(mapper);

    private WildlifeModelExtractionRequest request() {
        return WildlifeModelExtractionRequest.initial(
                WildlifeFormType.SIGHTING,
                "sighting-v1",
                "A ring-necked parakeet was seen near the feeder.",
                OffsetDateTime.parse("2026-07-30T10:30:00+04:00"),
                "en",
                "SYSTEM PROMPT BODY",
                "SIGHTING FORM PROMPT BODY",
                SCHEMA);
    }

    private OllamaChatModel modelReturning(String content) {
        OllamaChatModel chatModel = mock(OllamaChatModel.class);
        ChatResponse response = new ChatResponse(
                List.of(new Generation(new AssistantMessage(content))),
                ChatResponseMetadata.builder().model("llama3.1:8b").id("resp-1").build());
        when(chatModel.call(any(Prompt.class))).thenReturn(response);
        return chatModel;
    }

    private SpringAiOllamaStructuredWildlifeExtractionModel adapter(OllamaChatModel chatModel) {
        return adapter(chatModel, false);
    }

    private SpringAiOllamaStructuredWildlifeExtractionModel adapter(OllamaChatModel chatModel, boolean nativeSchemaFormat) {
        return new SpringAiOllamaStructuredWildlifeExtractionModel(
                chatModel, promptFactory, mapper, TestProperties.defaults(),
                new OllamaTuningProperties(nativeSchemaFormat, 8192, 2048, "30m"),
                new OllamaGrammarSchemaFactory(mapper), "test-model");
    }

    @Test
    void buildsSeparateSystemAndUserMessagesWithTranscriptDelimiters() {
        OllamaChatModel chatModel = modelReturning("{\"schemaVersion\":\"sighting-v1\"}");
        adapter(chatModel).extract(request());

        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        org.mockito.Mockito.verify(chatModel).call(captor.capture());
        List<Message> messages = captor.getValue().getInstructions();

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getMessageType()).isEqualTo(MessageType.SYSTEM);
        assertThat(messages.get(0).getText())
                .contains("SYSTEM PROMPT BODY")
                .contains("SIGHTING FORM PROMPT BODY")
                .doesNotContain("ring-necked parakeet");
        Message user = messages.get(1);
        assertThat(user.getMessageType()).isEqualTo(MessageType.USER);
        assertThat(user.getText())
                .contains("<transcript>")
                .contains("</transcript>")
                .contains("ring-necked parakeet")
                .contains("SIGHTING");
    }

    @Test
    void defaultsToGrammarSchemaFormatWithTemperatureZeroThinkingDisabledAndNoTools() {
        OllamaChatModel chatModel = modelReturning("{}");
        adapter(chatModel).extract(request());

        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        org.mockito.Mockito.verify(chatModel).call(captor.capture());
        OllamaChatOptions options = (OllamaChatOptions) captor.getValue().getOptions();

        assertThat(options.getTemperature()).isEqualTo(0.0d);
        assertThat(options.getFormat()).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> format = (Map<String, Object>) options.getFormat();
        assertThat(format).containsEntry("type", "object");
        assertThat(format).containsKey("properties");
        assertThat(options.getToolCallbacks()).isNullOrEmpty();
    }

    @Test
    void nativeSchemaFormatModeSendsTheFormSchemaAsFormat() {
        OllamaChatModel chatModel = modelReturning("{}");
        adapter(chatModel, true).extract(request());

        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        org.mockito.Mockito.verify(chatModel).call(captor.capture());
        OllamaChatOptions options = (OllamaChatOptions) captor.getValue().getOptions();

        assertThat(options.getFormat()).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> format = (Map<String, Object>) options.getFormat();
        assertThat(format).containsKey("type");
    }

    @Test
    void returnsSafeMetadataWithoutContent() {
        OllamaChatModel chatModel = modelReturning("{\"schemaVersion\":\"sighting-v1\"}");
        RawModelExtractionResponse response = adapter(chatModel).extract(request());

        assertThat(response.provider()).isEqualTo("ollama");
        assertThat(response.model()).isEqualTo("llama3.1:8b");
        assertThat(response.content()).contains("sighting-v1");
        assertThat(response.duration()).isNotNull();
        assertThat(response.safeMetadata())
                .containsEntry("model", "llama3.1:8b")
                .containsEntry("provider", "ollama");
        assertThat(response.safeMetadata().values())
                .noneMatch(v -> String.valueOf(v).contains("sighting-v1"));
    }

    @Test
    void rejectsBlankResponse() {
        assertThatThrownBy(() -> adapter(modelReturning("   ")).extract(request()))
                .isInstanceOf(MalformedModelOutputException.class);
    }

    @Test
    void rejectsOversizedResponse() {
        String big = "x".repeat(200);
        OllamaChatModel oversizedModel = modelReturning(big);
        SpringAiOllamaStructuredWildlifeExtractionModel adapter =
                new SpringAiOllamaStructuredWildlifeExtractionModel(
                        oversizedModel, promptFactory, mapper,
                        TestProperties.withMaximumModelResponseLength(50),
                        new OllamaTuningProperties(false, 8192, 2048, "30m"),
                        new OllamaGrammarSchemaFactory(mapper), "test-model");
        assertThatThrownBy(() -> adapter.extract(request()))
                .isInstanceOf(ModelOutputTooLargeException.class);
    }
}
