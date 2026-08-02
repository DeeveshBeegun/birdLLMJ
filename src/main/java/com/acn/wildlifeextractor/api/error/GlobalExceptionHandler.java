package com.acn.wildlifeextractor.api.error;

import java.time.Instant;

import com.acn.wildlifeextractor.application.error.CorrectionExhaustedException;
import com.acn.wildlifeextractor.application.error.ExtractionCapacityExceededException;
import com.acn.wildlifeextractor.application.error.ExtractionConfirmationException;
import com.acn.wildlifeextractor.application.error.ExtractionException;
import com.acn.wildlifeextractor.application.error.ExtractionNotFoundException;
import com.acn.wildlifeextractor.application.error.IdempotencyConflictException;
import com.acn.wildlifeextractor.application.error.InvalidExtractionRequestException;
import com.acn.wildlifeextractor.application.error.ModelTimeoutException;
import com.acn.wildlifeextractor.application.error.ModelUnavailableException;
import com.acn.wildlifeextractor.domain.form.UnsupportedWildlifeFormException;
import com.acn.wildlifeextractor.infrastructure.observability.ExtractionMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates exceptions into RFC 9457 {@link ProblemDetail} responses carrying a stable error
 * code, a safe title and detail, and a timestamp. Sensitive content (transcript, prompt, raw model
 * output, coordinates, identifiers, stack traces) is never included.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ExtractionMetrics metrics;

    public GlobalExceptionHandler(ExtractionMetrics metrics) {
        this.metrics = metrics;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleBeanValidation(MethodArgumentNotValidException ex) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_EXTRACTION_REQUEST",
                "Invalid request", "One or more request fields are invalid");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "Malformed request", "The request body could not be read");
    }

    @ExceptionHandler(InvalidExtractionRequestException.class)
    ProblemDetail handleInvalidRequest(InvalidExtractionRequestException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.errorCode(), "Invalid request", ex.getMessage());
    }

    @ExceptionHandler(UnsupportedWildlifeFormException.class)
    ProblemDetail handleUnsupportedForm(UnsupportedWildlifeFormException ex) {
        return problem(HttpStatus.BAD_REQUEST, "UNSUPPORTED_WILDLIFE_FORM",
                "Unsupported form", ex.getMessage());
    }

    @ExceptionHandler(ExtractionNotFoundException.class)
    ProblemDetail handleNotFound(ExtractionNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.errorCode(), "Extraction not found", ex.getMessage());
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    ProblemDetail handleConflict(IdempotencyConflictException ex) {
        return problem(HttpStatus.CONFLICT, ex.errorCode(), "Idempotency conflict", ex.getMessage());
    }

    @ExceptionHandler(ExtractionConfirmationException.class)
    ProblemDetail handleConfirmation(ExtractionConfirmationException ex) {
        return problem(HttpStatus.CONFLICT, ex.errorCode(), "Confirmation failed", ex.getMessage());
    }

    @ExceptionHandler(ModelTimeoutException.class)
    ProblemDetail handleTimeout(ModelTimeoutException ex) {
        return problem(HttpStatus.GATEWAY_TIMEOUT, ex.errorCode(), "Model timeout",
                "The extraction model did not respond in time");
    }

    @ExceptionHandler(ModelUnavailableException.class)
    ProblemDetail handleUnavailable(ModelUnavailableException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, ex.errorCode(), "Model unavailable",
                "The extraction model is currently unavailable");
    }

    @ExceptionHandler(ExtractionCapacityExceededException.class)
    ProblemDetail handleCapacity(ExtractionCapacityExceededException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, ex.errorCode(), "Capacity exceeded",
                "The service is at capacity; please retry later");
    }

    @ExceptionHandler(CorrectionExhaustedException.class)
    ProblemDetail handleCorrectionExhausted(CorrectionExhaustedException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.errorCode(), "Correction exhausted",
                "The model output could not be corrected");
    }

    @ExceptionHandler(ExtractionException.class)
    ProblemDetail handleExtraction(ExtractionException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.errorCode(), "Extraction failed",
                "The extraction could not be completed");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        // Top-level boundary: log the type only, never the message content, and return a generic error.
        log.error("Unhandled exception of type {}", ex.getClass().getName());
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Internal error", "An unexpected error occurred");
    }

    private ProblemDetail problem(HttpStatus status, String errorCode, String title, String detail) {
        metrics.recordFailure(errorCode);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("errorCode", errorCode);
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }
}
