package com.acn.wildlifeextractor.application.jev;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.acn.wildlifeextractor.application.validation.FieldStatusScanner.FieldOccurrence;
import com.acn.wildlifeextractor.application.validation.FieldStatusScanner.ScanResult;
import com.acn.wildlifeextractor.configuration.JevProperties;
import com.acn.wildlifeextractor.domain.vocabulary.DomainVocabularyProvider;
import com.acn.wildlifeextractor.domain.vocabulary.VocabularyDefinition;
import com.acn.wildlifeextractor.infrastructure.jev.JevApiClient;
import com.acn.wildlifeextractor.infrastructure.jev.JevApiClient.JevAnswer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Post-LLM correction stage that uses JEV (TypeSafe AI System One) to verify and, where
 * appropriate, override vocabulary-controlled field values extracted by the LLM.
 *
 * <p>For every PRESENT {@code ExtractedCode} field whose vocabulary has a known, bounded set of
 * allowed values, a JEV {@code choice} question is built and batched into a single API call.
 * If JEV returns a different value with confidence ≥ {@code confidenceThreshold}, the field's
 * {@code normalizedValue} in the live JSON tree is updated before vocabulary validation runs.
 * Fields with vocabularies larger than {@code maxVocabularySize} are skipped to avoid
 * overlong prompts.</p>
 *
 * <p>On any JEV failure the service is a no-op: corrections are best-effort.</p>
 */
@Service
public class JevExtractionCorrectionService {

    private static final Logger log = LoggerFactory.getLogger(JevExtractionCorrectionService.class);
    private static final String NONE_OF_THE_ABOVE = "none_of_the_above";

    private final JevApiClient jevClient;
    private final DomainVocabularyProvider vocabularyProvider;
    private final JevProperties properties;
    private final ObjectMapper objectMapper;

    public JevExtractionCorrectionService(JevApiClient jevClient,
                                          DomainVocabularyProvider vocabularyProvider,
                                          JevProperties properties,
                                          ObjectMapper objectMapper) {
        this.jevClient = jevClient;
        this.vocabularyProvider = vocabularyProvider;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Applies JEV corrections in-place to the live JSON nodes inside {@code scan}.
     * Does nothing if JEV is disabled.
     *
     * @param scan       the result of scanning the extracted {@code fields} JSON tree
     * @param transcript the original transcript text used as JEV context
     */
    public void correct(ScanResult scan, String transcript) {
        if (!properties.enabled()) {
            log.info("JEV is disabled, skipping correction");
            return;
        }

        // ── Build question map ────────────────────────────────────────────────
        Map<String, FieldOccurrence> questionKeyToField = new LinkedHashMap<>();
        Map<String, Object> questions = new LinkedHashMap<>();

        for (FieldOccurrence occurrence : scan.codeFields()) {
            if (!"PRESENT".equals(occurrence.status())) {
                continue;
            }
            var vocabNode = occurrence.node().get("vocabularyName");
            if (vocabNode == null || !vocabNode.isTextual()) {
                continue;
            }
            String vocabularyName = vocabNode.asText();
            Optional<VocabularyDefinition> vocabOpt = vocabularyProvider.find(vocabularyName);
            if (vocabOpt.isEmpty()) {
                continue;
            }
            VocabularyDefinition vocab = vocabOpt.get();
            List<String> allowed = List.copyOf(vocab.allowedValues());
            if (allowed.isEmpty() || allowed.size() > properties.maxVocabularySize()) {
                continue;
            }

            String questionKey = sanitizePath(occurrence.path());
            Map<String, String> criteria = new LinkedHashMap<>();
            for (String value : allowed) {
                criteria.put(value, value);
            }
            criteria.put(NONE_OF_THE_ABOVE, "The value is not clearly stated in the transcript");

            questions.put(questionKey, Map.of(
                    "type", "choice",
                    "instructions", "Based on the transcript, what is the value for '" + occurrence.path() + "'?",
                    "criteria", criteria
            ));
            questionKeyToField.put(questionKey, occurrence);
        }

        log.info("JEV built {} question(s) from {} scanned field(s)", questions.size(), scan.codeFields().size());

        if (questions.isEmpty()) {
            return;
        }

        // ── Call JEV ─────────────────────────────────────────────────────────
        Map<String, Object> state = Map.of("transcript", transcript);
        log.info("JEV request: questions={}", toJson(questions));
        Map<String, JevAnswer> answers = jevClient.ask(state, questions);
        log.info("JEV response: answers={}", toJson(answers));

        if (answers.isEmpty()) {
            return;
        }

        // ── Apply corrections ─────────────────────────────────────────────────
        int corrected = 0;
        for (Map.Entry<String, JevAnswer> entry : answers.entrySet()) {
            String questionKey = entry.getKey();
            JevAnswer answer = entry.getValue();

            if (answer.confidence() == null || answer.confidence() < properties.confidenceThreshold()) {
                continue;
            }
            String jevChoice = answer.choice();
            if (jevChoice == null || NONE_OF_THE_ABOVE.equals(jevChoice)) {
                continue;
            }

            FieldOccurrence occurrence = questionKeyToField.get(questionKey);
            if (occurrence == null) {
                continue;
            }

            var normalizedNode = occurrence.node().get("normalizedValue");
            String currentValue = normalizedNode != null ? normalizedNode.asText() : null;

            String normalizedJevChoice = jevChoice.toLowerCase(Locale.ROOT);
            if (normalizedJevChoice.equals(currentValue)) {
                continue;
            }

            if (occurrence.node() instanceof ObjectNode objectNode) {
                String originalRaw = occurrence.node().has("rawValue")
                        ? occurrence.node().get("rawValue").asText() : currentValue;
                log.info("JEV correction at '{}': '{}' → '{}' (confidence={:.2f})",
                        occurrence.path(), currentValue, normalizedJevChoice, answer.confidence());
                objectNode.put("normalizedValue", normalizedJevChoice);
                objectNode.put("rawValue", originalRaw + " [jev-corrected from: " + currentValue + "]");
                corrected++;
            }
        }

        if (corrected > 0) {
            log.info("JEV applied {} correction(s) to extracted fields", corrected);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return value.toString();
        }
    }

    private String sanitizePath(String path) {
        return path.replaceAll("[.\\[\\]]", "_");
    }
}
