package com.acn.wildlifeextractor.infrastructure.springai.ollama;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.acn.wildlifeextractor.application.error.MalformedModelOutputException;
import com.acn.wildlifeextractor.application.error.ModelOutputTooLargeException;
import com.acn.wildlifeextractor.application.extraction.RawModelExtractionResponse;
import com.acn.wildlifeextractor.application.extraction.StructuredWildlifeExtractionModel;
import com.acn.wildlifeextractor.application.extraction.WildlifeModelExtractionRequest;
import com.acn.wildlifeextractor.configuration.ExtractionProperties;
import com.acn.wildlifeextractor.configuration.OllamaTuningProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.ThinkOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Spring AI Ollama implementation of {@link StructuredWildlifeExtractionModel}.
 *
 * <p>All Spring AI and Ollama types are confined to this class. Temperature is zero for
 * determinism and model thinking is disabled. The response is received as raw text; it is neither
 * parsed nor trusted here beyond blank and size guards.</p>
 *
 * <p>Structured output mode is controlled by
 * {@code application.extraction.ollama-native-schema-format}. When {@code false} (the default) the
 * request uses Ollama's generic JSON mode ({@code format: "json"}); the strict form schema is then
 * enforced downstream by the validation pipeline. When {@code true} the full JSON Schema is sent as
 * the native {@code format}. Ollama's grammar engine (llama.cpp) cannot compile schemas that use
 * {@code $ref}/{@code $defs}/{@code allOf}/{@code if-then}, so native mode only works with servers
 * and schemas that support it; otherwise Ollama rejects the request with a grammar-parse error.</p>
 */
@Component
public class SpringAiOllamaStructuredWildlifeExtractionModel implements StructuredWildlifeExtractionModel {

    private static final Logger log = LoggerFactory.getLogger(SpringAiOllamaStructuredWildlifeExtractionModel.class);
    private static final String PROVIDER = "ollama";
    private static final String GENERIC_JSON_FORMAT = "json";

    private final ChatModel chatModel;
    private final OllamaExtractionPromptFactory promptFactory;
    private final ObjectMapper extractionObjectMapper;
    private final ExtractionProperties properties;
    private final OllamaTuningProperties tuning;
    private final String chatModelName;

    public SpringAiOllamaStructuredWildlifeExtractionModel(
            ChatModel chatModel,
            OllamaExtractionPromptFactory promptFactory,
            ObjectMapper extractionObjectMapper,
            ExtractionProperties properties,
            OllamaTuningProperties tuning,
            @Value("${spring.ai.ollama.chat.options.model:llama3.2:3b}") String chatModelName) {
        this.chatModel = chatModel;
        this.promptFactory = promptFactory;
        this.extractionObjectMapper = extractionObjectMapper;
        this.properties = properties;
        this.tuning = tuning;
        this.chatModelName = chatModelName;
    }

    @Override
    public RawModelExtractionResponse extract(WildlifeModelExtractionRequest request) {
        List<Message> messages = List.of(promptFactory.systemMessage(request), promptFactory.userMessage(request));
        Prompt prompt = new Prompt(messages, buildOptions(request));

        log.info("Extraction model call starting: provider={} model={} baseModel={} form={} purpose={}",
                PROVIDER, chatModelName, chatModelName, request.formType(), request.purpose());
        long startNanos = System.nanoTime();
        ChatResponse response = chatModel.call(prompt);
        Duration duration = Duration.ofNanos(System.nanoTime() - startNanos);

        String content = extractContent(response);
        guardResponseSize(content);

        String model = resolveModel(response);
        RawModelExtractionResponse result =
                new RawModelExtractionResponse(content, PROVIDER, model, duration, safeMetadata(response, model));
        log.info("Extraction model call completed form={} purpose={} provider={} model={} durationMs={}",
                request.formType(), request.purpose(), PROVIDER, model, duration.toMillis());
        return result;
    }

    private OllamaChatOptions buildOptions(WildlifeModelExtractionRequest request) {
        // Fluent chain: each builder call's return value is used, so a copy-style builder is
        // handled correctly. Temperature zero for determinism and thinking explicitly disabled.
        Object format = tuning.nativeSchemaFormat() ? parseSchema(request.jsonSchema()) : GENERIC_JSON_FORMAT;
        // numCtx: the output contract makes the prompt large, and Ollama's small default context
        // would overflow and fail the request on some models. numPredict caps worst-case latency;
        // keepAlive keeps the model resident so subsequent calls skip the reload cost.
        // Set the model explicitly so the request never depends on Spring AI's default-option
        // merge — the model that is logged is exactly the model that is sent.
        return OllamaChatOptions.builder()
                .model(chatModelName)
                .temperature(0.0d)
                .format(format)
                .numCtx(tuning.numCtx())
                .numPredict(tuning.numPredict())
                .keepAlive(tuning.keepAlive())
                .thinkOption(ThinkOption.ThinkBoolean.DISABLED)
                .build();
    }

    private Object parseSchema(String schemaJson) {
        try {
            return extractionObjectMapper.readValue(schemaJson, Map.class);
        } catch (JsonProcessingException e) {
            throw new MalformedModelOutputException("The form JSON Schema could not be parsed for model output", e);
        }
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
        return "unknown";
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
