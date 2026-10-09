package com.acn.wildlifeextractor.api.controller;

import com.acn.wildlifeextractor.api.request.ExtractionRequest;
import com.acn.wildlifeextractor.api.response.ConfirmationResponse;
import com.acn.wildlifeextractor.api.response.ExtractionResponse;
import com.acn.wildlifeextractor.application.confirmation.StoredExtraction;
import com.acn.wildlifeextractor.application.extraction.ExtractionCommand;
import com.acn.wildlifeextractor.application.extraction.WildlifeExtractionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for wildlife extraction and confirmation. Returns safe previews only; raw model output
 * is never exposed.
 */
@RestController
@RequestMapping("/api/v1/wildlife-extractions")
public class WildlifeExtractionController {

    private final WildlifeExtractionService extractionService;
    private final ObjectMapper extractionObjectMapper;

    public WildlifeExtractionController(WildlifeExtractionService extractionService,
                                        ObjectMapper extractionObjectMapper) {
        this.extractionService = extractionService;
        this.extractionObjectMapper = extractionObjectMapper;
    }

    @PostMapping
    public ResponseEntity<ExtractionResponse> extract(@Valid @RequestBody ExtractionRequest request) {
        ExtractionCommand command = new ExtractionCommand(request.requestId(), request.conversationId(),
                request.formType(), request.transcript(), request.transcriptTimestamp(), request.language(),
                request.userId(), request.time(), request.date(),
                request.latitude(), request.longitude(), request.subPopulation());
        return ResponseEntity.ok(toResponse(extractionService.extract(command)));
    }

    @PostMapping("/{requestId}/confirm")
    public ResponseEntity<ConfirmationResponse> confirm(@PathVariable String requestId) {
        StoredExtraction stored = extractionService.confirm(requestId);
        return ResponseEntity.ok(new ConfirmationResponse(
                stored.requestId(), stored.decision(), stored.confirmed(), stored.confirmedAt()));
    }

    private ExtractionResponse toResponse(StoredExtraction stored) {
        // Convert the Jackson 2 fields tree to a plain structure so the web (Jackson 3) mapper renders it.
        Object validatedFields = extractionObjectMapper.convertValue(stored.validatedFields(), Object.class);
        return new ExtractionResponse(
                stored.requestId(), stored.conversationId(), stored.formType(), stored.schemaVersion(),
                stored.promptVersion(), stored.decision(), validatedFields, stored.missingRequiredFields(),
                stored.ambiguousFields(), stored.invalidFields(), stored.unresolvedReferences(),
                stored.warnings(), stored.safeModelMetadata(), stored.modelCallCount(),
                stored.correctionAttemptCount(), stored.infrastructureRetryCount(),
                stored.userId(), stored.time(), stored.date(),
                stored.latitude(), stored.longitude(), stored.subPopulation());
    }
}
