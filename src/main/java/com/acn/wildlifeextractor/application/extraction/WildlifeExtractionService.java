package com.acn.wildlifeextractor.application.extraction;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.acn.wildlifeextractor.application.confirmation.ExtractionRepository;
import com.acn.wildlifeextractor.application.confirmation.StoredExtraction;
import com.acn.wildlifeextractor.application.error.ExtractionConfirmationException;
import com.acn.wildlifeextractor.application.error.ExtractionNotFoundException;
import com.acn.wildlifeextractor.application.error.IdempotencyConflictException;
import com.acn.wildlifeextractor.application.error.InvalidExtractionRequestException;
import com.acn.wildlifeextractor.application.error.MalformedModelOutputException;
import com.acn.wildlifeextractor.application.error.ModelOutputMappingException;
import com.acn.wildlifeextractor.application.error.ModelOutputTooLargeException;
import com.acn.wildlifeextractor.application.error.SchemaValidationException;
import com.acn.wildlifeextractor.application.validation.ExtractionValidationPipeline;
import com.acn.wildlifeextractor.application.validation.ValidatedExtraction;
import com.acn.wildlifeextractor.configuration.ExtractionProperties;
import com.acn.wildlifeextractor.domain.extraction.ExtractionDecision;
import com.acn.wildlifeextractor.domain.validation.SanitizedValidationError;
import com.acn.wildlifeextractor.domain.form.WildlifeFormDefinition;
import com.acn.wildlifeextractor.infrastructure.form.WildlifeFormDefinitionRegistry;
import com.acn.wildlifeextractor.infrastructure.observability.ExtractionMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

/**
 * Orchestrates a single extraction: request validation, one model call, the full validation
 * pipeline, and idempotent storage of the safe result. Structural output failures are converted
 * to a stored {@code REJECT} result; transport failures propagate to the caller.
 */
@Service
public class WildlifeExtractionService {

    private final WildlifeFormDefinitionRegistry registry;
    private final StructuredWildlifeExtractionModel model;
    private final ExtractionValidationPipeline pipeline;
    private final ExtractionRepository repository;
    private final ExtractionProperties properties;
    private final ObjectMapper objectMapper;
    private final ExtractionMetrics metrics;

    public WildlifeExtractionService(WildlifeFormDefinitionRegistry registry,
                                     StructuredWildlifeExtractionModel model,
                                     ExtractionValidationPipeline pipeline,
                                     ExtractionRepository repository,
                                     ExtractionProperties properties,
                                     ObjectMapper extractionObjectMapper,
                                     ExtractionMetrics metrics) {
        this.registry = registry;
        this.model = model;
        this.pipeline = pipeline;
        this.repository = repository;
        this.properties = properties;
        this.objectMapper = extractionObjectMapper;
        this.metrics = metrics;
    }

    public StoredExtraction extract(ExtractionCommand command) {
        long startNanos = System.nanoTime();
        StoredExtraction stored = doExtract(command);
        metrics.recordOutcome(stored.formType(), stored.decision(), stored.modelCallCount(),
                stored.correctionAttemptCount(), Duration.ofNanos(System.nanoTime() - startNanos));
        return stored;
    }

    private StoredExtraction doExtract(ExtractionCommand command) {
        validateRequest(command);
        WildlifeFormDefinition<?> definition = registry.getByFormType(command.formType());
        String fingerprint = TranscriptFingerprint.of(command.conversationId(),
                command.formType().name(), definition.schemaVersion(), command.transcript());

        var existing = repository.findById(command.requestId());
        if (existing.isPresent()) {
            return ensureSameFingerprint(existing.get(), fingerprint);
        }
        StoredExtraction stored = repository.computeIfAbsent(command.requestId(),
                () -> runExtraction(command, definition, fingerprint));
        return ensureSameFingerprint(stored, fingerprint);
    }

    public StoredExtraction confirm(String requestId) {
        StoredExtraction stored = repository.findById(requestId)
                .orElseThrow(() -> new ExtractionNotFoundException(requestId));
        if (stored.confirmed()) {
            return stored; // idempotent: already confirmed
        }
        if (stored.decision() == ExtractionDecision.REJECT) {
            throw new ExtractionConfirmationException("A rejected extraction cannot be confirmed");
        }
        if (registry.findBySchemaVersion(stored.schemaVersion()).isEmpty()) {
            throw new ExtractionConfirmationException("Schema version " + stored.schemaVersion() + " is no longer supported");
        }
        if (!stored.unresolvedReferences().isEmpty()) {
            throw new ExtractionConfirmationException("Unresolved references must be resolved before confirmation");
        }
        return repository.update(stored.asConfirmed(Instant.now()));
    }

    private void validateRequest(ExtractionCommand command) {
        if (command.transcript() == null || command.transcript().isBlank()) {
            throw new InvalidExtractionRequestException("Transcript must not be blank");
        }
        if (command.transcript().length() > properties.maximumTranscriptLength()) {
            throw new InvalidExtractionRequestException("Transcript exceeds the maximum allowed length");
        }
    }

    private StoredExtraction ensureSameFingerprint(StoredExtraction stored, String fingerprint) {
        if (!stored.transcriptFingerprint().equals(fingerprint)) {
            throw new IdempotencyConflictException(stored.requestId());
        }
        return stored;
    }

    private StoredExtraction runExtraction(ExtractionCommand command, WildlifeFormDefinition<?> definition,
                                           String fingerprint) {
        WildlifeModelExtractionRequest initial = WildlifeModelExtractionRequest.initial(
                command.formType(), definition.schemaVersion(), command.transcript(),
                command.transcriptTimestamp(), command.language(),
                definition.systemPrompt(), definition.formPrompt(), definition.jsonSchema().toString());

        RawModelExtractionResponse raw;
        try {
            raw = model.extract(initial);
        } catch (MalformedModelOutputException | ModelOutputTooLargeException e) {
            // Blank or oversized output is not structurally repairable; reject safely.
            return toRejected(command, definition, fingerprint, "unknown", Map.of(), 1, 0);
        }

        ValidationOutcome first = tryValidate(definition, raw.content(), command.transcript());
        if (first.success()) {
            return toStored(command, definition, fingerprint, raw, first.validated(), 1, 0);
        }
        if (properties.maximumCorrectionAttempts() <= 0) {
            return toRejected(command, definition, fingerprint, raw.model(), raw.safeMetadata(), 1, 0);
        }

        // Single structural correction attempt.
        WildlifeModelExtractionRequest correction = initial.asCorrection(raw.content(), first.errorSummary());
        RawModelExtractionResponse corrected;
        try {
            corrected = model.extract(correction);
        } catch (MalformedModelOutputException | ModelOutputTooLargeException e) {
            return toRejected(command, definition, fingerprint, raw.model(), raw.safeMetadata(), 2, 1);
        }

        ValidationOutcome second = tryValidate(definition, corrected.content(), command.transcript());
        if (second.success()) {
            return toStored(command, definition, fingerprint, corrected, second.validated(), 2, 1);
        }
        return toRejected(command, definition, fingerprint, corrected.model(), corrected.safeMetadata(), 2, 1);
    }

    private ValidationOutcome tryValidate(WildlifeFormDefinition<?> definition, String content, String transcript) {
        try {
            return new ValidationOutcome(true, pipeline.validate(definition, content, transcript), null);
        } catch (SchemaValidationException e) {
            return new ValidationOutcome(false, null, summarize(e.errors()));
        } catch (ModelOutputMappingException | MalformedModelOutputException e) {
            return new ValidationOutcome(false, null, "The output did not match the required structure");
        }
    }

    private String summarize(List<SanitizedValidationError> errors) {
        return errors.stream().limit(10).map(SanitizedValidationError::safeDescription)
                .reduce((a, b) -> a + "; " + b).orElse("Schema validation failed");
    }

    private StoredExtraction toStored(ExtractionCommand command, WildlifeFormDefinition<?> definition,
                                      String fingerprint, RawModelExtractionResponse raw,
                                      ValidatedExtraction validated, int modelCallCount, int correctionAttempts) {
        return new StoredExtraction(command.requestId(), command.conversationId(), command.formType(),
                definition.schemaVersion(), definition.promptVersion(), validated.decision(),
                validated.validatedFields(), validated.missingRequiredFields(), validated.ambiguousFields(),
                validated.invalidFields(), validated.unresolvedReferences(), validated.warnings(),
                raw.safeMetadata(), raw.model(), modelCallCount, correctionAttempts, 0, fingerprint,
                Instant.now(), null, false);
    }

    private StoredExtraction toRejected(ExtractionCommand command, WildlifeFormDefinition<?> definition,
                                        String fingerprint, String modelName, Map<String, Object> metadata,
                                        int modelCallCount, int correctionAttempts) {
        return new StoredExtraction(command.requestId(), command.conversationId(), command.formType(),
                definition.schemaVersion(), definition.promptVersion(), ExtractionDecision.REJECT,
                objectMapper.createObjectNode(), List.of(), List.of(), List.of(), List.of(),
                List.of("Model output rejected"), metadata, modelName,
                modelCallCount, correctionAttempts, 0, fingerprint, Instant.now(), null, false);
    }

    /** Internal result of a validation attempt within the correction flow. */
    private record ValidationOutcome(boolean success, ValidatedExtraction validated, String errorSummary) {
    }
}
