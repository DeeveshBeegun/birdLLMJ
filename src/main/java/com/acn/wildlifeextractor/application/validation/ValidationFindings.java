package com.acn.wildlifeextractor.application.validation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Mutable accumulator for the findings produced across the validation pipeline. Field-name
 * collections are de-duplicated and ordered; contradictions and manual-review triggers are kept
 * separately so the deterministic decision service can classify the outcome.
 */
public class ValidationFindings {

    private final Set<String> missingRequiredFields = new LinkedHashSet<>();
    private final Set<String> ambiguousFields = new LinkedHashSet<>();
    private final Set<String> invalidFields = new LinkedHashSet<>();
    private final Set<String> unresolvedReferences = new LinkedHashSet<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> contradictions = new ArrayList<>();
    private final List<String> manualReviewReasons = new ArrayList<>();

    public void addMissingRequiredField(String field) {
        missingRequiredFields.add(field);
    }

    public void addAmbiguousField(String field) {
        ambiguousFields.add(field);
    }

    public void addInvalidField(String field) {
        invalidFields.add(field);
    }

    public void addUnresolvedReference(String field) {
        unresolvedReferences.add(field);
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    /** Records a hard contradiction between spoken values; always forces manual review. */
    public void addContradiction(String reason) {
        contradictions.add(reason);
        manualReviewReasons.add(reason);
    }

    /** Records a reason the extraction should be routed to a human without being a contradiction. */
    public void addManualReviewReason(String reason) {
        manualReviewReasons.add(reason);
    }

    public List<String> missingRequiredFields() {
        return List.copyOf(missingRequiredFields);
    }

    public List<String> ambiguousFields() {
        return List.copyOf(ambiguousFields);
    }

    public List<String> invalidFields() {
        return List.copyOf(invalidFields);
    }

    public List<String> unresolvedReferences() {
        return List.copyOf(unresolvedReferences);
    }

    public List<String> warnings() {
        return List.copyOf(warnings);
    }

    public List<String> contradictions() {
        return List.copyOf(contradictions);
    }

    public List<String> manualReviewReasons() {
        return List.copyOf(manualReviewReasons);
    }

    public boolean hasMissingRequiredFields() {
        return !missingRequiredFields.isEmpty();
    }

    public boolean hasAmbiguousFields() {
        return !ambiguousFields.isEmpty();
    }

    public boolean hasInvalidFields() {
        return !invalidFields.isEmpty();
    }

    public boolean hasUnresolvedReferences() {
        return !unresolvedReferences.isEmpty();
    }

    public boolean requiresManualReview() {
        return !manualReviewReasons.isEmpty();
    }
}
