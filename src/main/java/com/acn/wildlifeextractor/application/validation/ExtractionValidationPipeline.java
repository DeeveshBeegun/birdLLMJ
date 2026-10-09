package com.acn.wildlifeextractor.application.validation;

import java.util.Set;

import com.acn.wildlifeextractor.application.decision.ExtractionDecisionService;
import com.acn.wildlifeextractor.application.error.SchemaValidationException;
import com.acn.wildlifeextractor.application.jev.JevExtractionCorrectionService;
import com.acn.wildlifeextractor.application.reference.ReferenceResolutionStage;
import com.acn.wildlifeextractor.application.validation.FieldStatusScanner.ScanResult;
import com.acn.wildlifeextractor.application.vocabulary.VocabularyValidator;
import com.acn.wildlifeextractor.configuration.ExtractionProperties;
import com.acn.wildlifeextractor.domain.extraction.ExtractionDecision;
import com.acn.wildlifeextractor.domain.extraction.ExtractionStatus;
import com.acn.wildlifeextractor.domain.form.WildlifeFormDefinition;
import com.acn.wildlifeextractor.domain.validation.SchemaValidationResult;
import com.acn.wildlifeextractor.domain.validation.StructuredOutputSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

/**
 * Runs the validation pipeline in the mandated order:
 * <pre>
 * JSON parsing → JSON Schema validation → DTO mapping → Jakarta Bean Validation
 * → status scan → JEV correction → vocabulary validation → evidence matching
 * → reference resolution → business validation → required-field check → deterministic decision
 * </pre>
 * Structural failures (malformed JSON, schema violations, mapping failures) are thrown as
 * exceptions so the orchestrator can decide whether to attempt a single correction. All other
 * outcomes are recorded as findings and classified into a decision.
 */
@Component
public class ExtractionValidationPipeline {

    private final ModelOutputParser parser;
    private final StructuredOutputSchemaValidator schemaValidator;
    private final ExtractionDtoMapper dtoMapper;
    private final Validator beanValidator;
    private final FieldStatusScanner fieldStatusScanner;
    private final JevExtractionCorrectionService jevCorrectionService;
    private final VocabularyValidator vocabularyValidator;
    private final EvidenceValidator evidenceValidator;
    private final ReferenceResolutionStage referenceResolutionStage;
    private final BusinessValidator businessValidator;
    private final RequiredFieldsCatalog requiredFieldsCatalog;
    private final ExtractionDecisionService decisionService;
    private final ExtractionProperties properties;

    public ExtractionValidationPipeline(ModelOutputParser parser,
                                        StructuredOutputSchemaValidator schemaValidator,
                                        ExtractionDtoMapper dtoMapper,
                                        Validator beanValidator,
                                        FieldStatusScanner fieldStatusScanner,
                                        JevExtractionCorrectionService jevCorrectionService,
                                        VocabularyValidator vocabularyValidator,
                                        EvidenceValidator evidenceValidator,
                                        ReferenceResolutionStage referenceResolutionStage,
                                        BusinessValidator businessValidator,
                                        RequiredFieldsCatalog requiredFieldsCatalog,
                                        ExtractionDecisionService decisionService,
                                        ExtractionProperties properties) {
        this.parser = parser;
        this.schemaValidator = schemaValidator;
        this.dtoMapper = dtoMapper;
        this.beanValidator = beanValidator;
        this.fieldStatusScanner = fieldStatusScanner;
        this.jevCorrectionService = jevCorrectionService;
        this.vocabularyValidator = vocabularyValidator;
        this.evidenceValidator = evidenceValidator;
        this.referenceResolutionStage = referenceResolutionStage;
        this.businessValidator = businessValidator;
        this.requiredFieldsCatalog = requiredFieldsCatalog;
        this.decisionService = decisionService;
        this.properties = properties;
    }

    public ValidatedExtraction validate(WildlifeFormDefinition<?> definition, String rawContent, String transcript) {
        JsonNode envelope = parser.parse(rawContent);

        SchemaValidationResult schemaResult = schemaValidator.validate(envelope, definition.jsonSchema());
        if (!schemaResult.valid()) {
            throw new SchemaValidationException(schemaResult.errors());
        }

        JsonNode fields = envelope.get("fields");
        Object dto = dtoMapper.map(fields, definition.extractionType());

        ValidationFindings findings = new ValidationFindings();
        runBeanValidation(dto, findings);

        ScanResult scan = fieldStatusScanner.scan(fields);
        fieldStatusScanner.collectStatusFindings(scan, findings);

        // JEV correction: override high-confidence mismatches before vocabulary validation.
        jevCorrectionService.correct(scan, transcript);

        vocabularyValidator.validate(scan, findings, properties.strictVocabularyValidation());
        if (properties.evidenceCheckEnabled()) {
            evidenceValidator.validate(scan, transcript, findings);
        }
        referenceResolutionStage.resolve(definition.formType(), fields, scan, findings);
        businessValidator.validate(definition.formType(), fields, findings);
        collectMissingRequiredFields(definition, fields, findings);

        ExtractionDecision decision = decisionService.decide(findings);
        return new ValidatedExtraction(
                definition.formType(),
                definition.schemaVersion(),
                definition.promptVersion(),
                readExtractionStatus(envelope),
                fields,
                findings.missingRequiredFields(),
                findings.ambiguousFields(),
                findings.invalidFields(),
                findings.unresolvedReferences(),
                findings.warnings(),
                decision);
    }

    private void runBeanValidation(Object dto, ValidationFindings findings) {
        for (ConstraintViolation<Object> violation : beanValidator.validate(dto)) {
            findings.addInvalidField(violation.getPropertyPath().toString());
        }
    }

    private void collectMissingRequiredFields(WildlifeFormDefinition<?> definition, JsonNode fields,
                                              ValidationFindings findings) {
        Set<String> required = requiredFieldsCatalog.requiredFields(definition.formType());
        FieldsAccessor accessor = new FieldsAccessor(fields);
        for (String field : required) {
            String status = accessor.status(field);
            if (status == null || "MISSING".equals(status)) {
                findings.addMissingRequiredField(field);
            }
        }
    }

    private ExtractionStatus readExtractionStatus(JsonNode envelope) {
        JsonNode status = envelope.get("extractionStatus");
        if (status != null && status.isTextual()) {
            try {
                return ExtractionStatus.valueOf(status.asText());
            } catch (IllegalArgumentException ignored) {
                // Fall through to the safe default below.
            }
        }
        return ExtractionStatus.INCOMPLETE;
    }
}
