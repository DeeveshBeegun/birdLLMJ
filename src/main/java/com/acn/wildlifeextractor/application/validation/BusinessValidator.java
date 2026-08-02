package com.acn.wildlifeextractor.application.validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import com.acn.wildlifeextractor.configuration.ExtractionProperties;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

/**
 * Deterministic wildlife business validation. Operates on the validated {@code fields} tree,
 * recording invalid values, contradictions and manual-review triggers. Everything here is
 * computed in Java; the model is never consulted for these judgements.
 */
@Component
public class BusinessValidator {

    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-90");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90");
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180");
    private static final BigDecimal MAX_BEARING_EXCLUSIVE = new BigDecimal("360");

    private final ExtractionProperties properties;

    public BusinessValidator(ExtractionProperties properties) {
        this.properties = properties;
    }

    public void validate(WildlifeFormType formType, JsonNode fields, ValidationFindings findings) {
        FieldsAccessor accessor = new FieldsAccessor(fields);
        validateShared(accessor, findings);
        switch (formType) {
            case SIGHTING -> validateSighting(accessor, findings);
            case FEEDING_OBSERVATION -> validateFeeding(accessor, findings);
            case NEST_SITE_CHARACTERISTICS -> validateNestSite(accessor, findings);
            case COMPETITORS_AND_PREDATORS -> validateCompetitors(accessor, findings);
            case NEST_EGGS_CHICK -> validateNestEggsChick(accessor, fields, findings);
            case RINGING_MORPHS -> validateRinging(accessor, findings);
        }
    }

    private void validateShared(FieldsAccessor accessor, ValidationFindings findings) {
        rejectOutOfRange(accessor, "latitude", MIN_LATITUDE, MAX_LATITUDE, true, findings);
        rejectOutOfRange(accessor, "longitude", MIN_LONGITUDE, MAX_LONGITUDE, true, findings);
        validateTimeOrder(accessor, "startTime", "endTime", findings);
    }

    private void validateSighting(FieldsAccessor accessor, ValidationFindings findings) {
        rejectNegative(accessor, "suspectedPairNumber", findings);
        if (accessor.isPresent("causeOfDeath") || accessor.isPresent("stageOfDecomposition")) {
            findings.addManualReviewReason("Mortality details require review of the sighting context");
        }
        validatePhysicalCondition(accessor, findings);
    }

    private void validateFeeding(FieldsAccessor accessor, ValidationFindings findings) {
        // foragingHeight and treeHeight are intentionally free text (units undefined); no numeric check.
        if (accessor.isPresent("treeSpecies") && !accessor.isPresent("treeStatus")
                && !accessor.isPresent("treeHeight") && !accessor.isPresent("foragingHeight")) {
            findings.addWarning("Tree species present without any other tree detail");
        }
    }

    private void validateNestSite(FieldsAccessor accessor, ValidationFindings findings) {
        if (accessor.isPresent("bearing")) {
            BigDecimal bearing = accessor.decimalValue("bearing");
            if (bearing != null && (bearing.signum() < 0 || bearing.compareTo(MAX_BEARING_EXCLUSIVE) >= 0)) {
                findings.addInvalidField("bearing");
            }
        }
        rejectNegative(accessor, "entranceDiameter", findings);
        rejectNegative(accessor, "cavityDepth", findings);
        rejectNegative(accessor, "heightAboveNestBowl", findings);
    }

    private void validateCompetitors(FieldsAccessor accessor, ValidationFindings findings) {
        rejectNegative(accessor, "numOfCompetitor", findings);
        rejectNegative(accessor, "distanceFromNest", findings);
        Boolean hasCompetitorInfo = accessor.booleanValue("hasCompetitorInfo");
        boolean competitorDetailsPresent = accessor.isPresent("competitorSpecies")
                || accessor.isPresent("numOfCompetitor")
                || accessor.isPresent("behaviour")
                || accessor.isPresent("location");
        if (Boolean.FALSE.equals(hasCompetitorInfo) && competitorDetailsPresent) {
            findings.addContradiction("Competitor details present but hasCompetitorInfo is false");
        }
    }

    private void validateNestEggsChick(FieldsAccessor accessor, JsonNode fields, ValidationFindings findings) {
        JsonNode eggs = fields == null ? null : fields.get("eggDetails");
        JsonNode chicks = fields == null ? null : fields.get("chickDetails");
        if (eggs != null && eggs.isArray() && eggs.size() > properties.maximumEggsPerObservation()) {
            findings.addInvalidField("eggDetails");
        }
        if (chicks != null && chicks.isArray() && chicks.size() > properties.maximumChicksPerObservation()) {
            findings.addInvalidField("chickDetails");
        }
        flagDuplicateIndividualNumbers(eggs, "eggDetails", findings);
        flagDuplicateIndividualNumbers(chicks, "chickDetails", findings);
        rejectNegativeNestedNumbers(chicks, "chickDetails", findings);
        validateHatchNotBeforeLaying(eggs, chicks, findings);
    }

    private void validateRinging(FieldsAccessor accessor, ValidationFindings findings) {
        for (String measurement : new String[]{"apparentAge", "weight", "tail", "tailBrush", "tarsusLength",
                "wing", "p8", "p8Brush", "head", "culmenToGape", "culmenFeather", "culmenToSkull"}) {
            rejectNegative(accessor, measurement, findings);
        }
        validatePhysicalCondition(accessor, findings);
    }

    private void validatePhysicalCondition(FieldsAccessor accessor, ValidationFindings findings) {
        JsonNode physicalCondition = accessor.raw("physicalCondition");
        if (physicalCondition == null || !physicalCondition.isObject()) {
            return;
        }
        FieldsAccessor pc = new FieldsAccessor(physicalCondition);
        Boolean hasClinicalSigns = pc.booleanValue("hasClinicalSigns");
        JsonNode clinicalSigns = pc.codeListValues("clinicalSigns");
        boolean clinicalSignsListed = clinicalSigns != null && clinicalSigns.isArray() && !clinicalSigns.isEmpty();
        if (Boolean.FALSE.equals(hasClinicalSigns) && clinicalSignsListed) {
            findings.addManualReviewReason("Clinical signs listed while hasClinicalSigns is false");
        }
        Boolean hasInjury = pc.booleanValue("hasInjury");
        if (Boolean.FALSE.equals(hasInjury) && pc.isPresent("injurySeverity")) {
            findings.addManualReviewReason("Injury severity present while hasInjury is false");
        }
    }

    private void rejectOutOfRange(FieldsAccessor accessor, String field, BigDecimal min, BigDecimal max,
                                  boolean inclusiveMax, ValidationFindings findings) {
        if (!accessor.isPresent(field)) {
            return;
        }
        BigDecimal value = accessor.decimalValue(field);
        if (value == null) {
            return;
        }
        boolean belowMin = value.compareTo(min) < 0;
        boolean aboveMax = inclusiveMax ? value.compareTo(max) > 0 : value.compareTo(max) >= 0;
        if (belowMin || aboveMax) {
            findings.addInvalidField(field);
        }
    }

    private void rejectNegative(FieldsAccessor accessor, String field, ValidationFindings findings) {
        if (!accessor.isPresent(field)) {
            return;
        }
        BigDecimal value = accessor.decimalValue(field);
        if (value != null && value.signum() < 0) {
            findings.addInvalidField(field);
        }
    }

    private void validateTimeOrder(FieldsAccessor accessor, String startField, String endField,
                                   ValidationFindings findings) {
        LocalTime start = parseTime(accessor.textValue(startField));
        LocalTime end = parseTime(accessor.textValue(endField));
        if (start != null && end != null && start.isAfter(end)) {
            findings.addContradiction("Start time is after end time");
        }
    }

    private void flagDuplicateIndividualNumbers(JsonNode entries, String listName, ValidationFindings findings) {
        if (entries == null || !entries.isArray()) {
            return;
        }
        Set<String> seen = new HashSet<>();
        for (JsonNode entry : entries) {
            JsonNode individual = entry.path("individualNumber").path("normalizedValue");
            if (individual.isTextual() && !individual.asText().isBlank() && !seen.add(individual.asText())) {
                findings.addManualReviewReason("Duplicate individual number in " + listName);
            }
        }
    }

    private void rejectNegativeNestedNumbers(JsonNode entries, String listName, ValidationFindings findings) {
        if (entries == null || !entries.isArray()) {
            return;
        }
        for (String field : new String[]{"age", "weight"}) {
            for (JsonNode entry : entries) {
                JsonNode value = entry.path(field).path("normalizedValue");
                if (value.isNumber() && value.decimalValue().signum() < 0) {
                    findings.addInvalidField(listName + "." + field);
                }
            }
        }
    }

    private void validateHatchNotBeforeLaying(JsonNode eggs, JsonNode chicks, ValidationFindings findings) {
        if (eggs == null || !eggs.isArray() || chicks == null || !chicks.isArray()) {
            return;
        }
        for (JsonNode chick : chicks) {
            String individual = chick.path("individualNumber").path("normalizedValue").asText(null);
            LocalDate hatch = parseDate(chick.path("hatchDate").path("normalizedValue").asText(null));
            if (individual == null || hatch == null) {
                continue;
            }
            for (JsonNode egg : eggs) {
                String eggIndividual = egg.path("individualNumber").path("normalizedValue").asText(null);
                LocalDate laid = parseDate(egg.path("dateLaid").path("normalizedValue").asText(null));
                if (individual.equals(eggIndividual) && laid != null && hatch.isBefore(laid)) {
                    findings.addContradiction("Hatch date precedes laying date for individual " + individual);
                }
            }
        }
    }

    private LocalTime parseTime(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalTime.parse(value);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
