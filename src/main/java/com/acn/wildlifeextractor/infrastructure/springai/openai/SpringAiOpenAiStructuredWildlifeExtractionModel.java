package com.acn.wildlifeextractor.infrastructure.springai.openai;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.acn.wildlifeextractor.application.error.MalformedModelOutputException;
import com.acn.wildlifeextractor.application.error.ModelOutputTooLargeException;
import com.acn.wildlifeextractor.application.extraction.RawModelExtractionResponse;
import com.acn.wildlifeextractor.application.extraction.StructuredWildlifeExtractionModel;
import com.acn.wildlifeextractor.application.extraction.WildlifeModelExtractionRequest;
import com.acn.wildlifeextractor.configuration.ExtractionProperties;
import com.acn.wildlifeextractor.infrastructure.springai.ollama.OllamaExtractionPromptFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Spring AI OpenAI implementation of {@link StructuredWildlifeExtractionModel}, wired to
 * OpenRouter's OpenAI-compatible API ({@code spring.ai.openai.base-url}).
 *
 * <p>Active when {@code application.extraction.provider=openai} (the default). The prompt
 * already carries an explicit JSON output contract, so no additional response-format
 * constraint is sent — the model follows the contract from the user message.</p>
 */
@Component
@Qualifier("rawExtractionModel")
@ConditionalOnProperty(name = "application.extraction.provider", havingValue = "openai", matchIfMissing = true)
public class SpringAiOpenAiStructuredWildlifeExtractionModel implements StructuredWildlifeExtractionModel {

    private static final Logger log = LoggerFactory.getLogger(SpringAiOpenAiStructuredWildlifeExtractionModel.class);
    private static final String PROVIDER = "openrouter";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final OpenAiChatModel chatModel;
    private final OllamaExtractionPromptFactory promptFactory;
    private final ExtractionProperties properties;
    private final String chatModelName;

    public SpringAiOpenAiStructuredWildlifeExtractionModel(
            OpenAiChatModel chatModel,
            OllamaExtractionPromptFactory promptFactory,
            ExtractionProperties properties,
            @Value("${spring.ai.openai.chat.options.model:openai/gpt-4o-mini}") String chatModelName) {
        this.chatModel = chatModel;
        this.promptFactory = promptFactory;
        this.properties = properties;
        this.chatModelName = chatModelName;
    }

    @Override
    public RawModelExtractionResponse extract(WildlifeModelExtractionRequest request) {
        List<Message> messages = List.of(promptFactory.systemMessage(request), promptFactory.userMessage(request));
        Prompt prompt = new Prompt(messages, buildOptions());

        log.info("Extraction model call starting: provider={} model={} form={} purpose={}",
                PROVIDER, chatModelName, request.formType(), request.purpose());
        long startNanos = System.nanoTime();
        ChatResponse response = chatModel.call(prompt);
        Duration duration = Duration.ofNanos(System.nanoTime() - startNanos);

        String content = extractContent(response);
        log.info("Raw model output:\n{}", prettyJson(content));
        guardResponseSize(content);

        String model = resolveModel(response);
        RawModelExtractionResponse result =
                new RawModelExtractionResponse(content, PROVIDER, model, duration, safeMetadata(response, model));
        log.info("Extraction model call completed form={} purpose={} provider={} model={} durationMs={}",
                request.formType(), request.purpose(), PROVIDER, model, duration.toMillis());
        return result;
    }

    private static String prettyJson(String raw) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(MAPPER.readTree(raw));
        } catch (Exception e) {
            return raw;
        }
    }

    private OpenAiChatOptions buildOptions() {
        return OpenAiChatOptions.builder()
                .model(chatModelName)
                .temperature(0.0)
                .responseFormat(OpenAiChatModel.ResponseFormat.builder()
                        .type(OpenAiChatModel.ResponseFormat.Type.JSON_OBJECT)
                        .build())
                .build();
    }

    private String extractContent(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            throw new MalformedModelOutputException("The model returned no result");
        }
        String content = response.getResult().getOutput().getText();
        if (content == null || content.isBlank()) {
            throw new MalformedModelOutputException("The model returned a blank response");
        }
        return content;
    }

    private void guardResponseSize(String content) {
        int max = properties.maximumModelResponseLength();
        if (content.length() > max) {
            throw new ModelOutputTooLargeException(content.length(), max);
        }
    }

    private String resolveModel(ChatResponse response) {
        if (response.getMetadata() != null && response.getMetadata().getModel() != null
                && !response.getMetadata().getModel().isBlank()) {
            return response.getMetadata().getModel();
        }
        return chatModelName;
    }

    private Map<String, Object> safeMetadata(ChatResponse response, String model) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("provider", PROVIDER);
        metadata.put("model", model);
        if (response.getMetadata() != null && response.getMetadata().getId() != null) {
            metadata.put("responseId", response.getMetadata().getId());
        }
        return metadata;
    }
}
