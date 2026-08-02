package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.field.ExtractedBoolean;
import com.acn.wildlifeextractor.domain.field.ExtractedCode;
import com.acn.wildlifeextractor.domain.field.ExtractedCodeList;
import com.acn.wildlifeextractor.domain.field.ExtractedDate;
import com.acn.wildlifeextractor.domain.field.ExtractedDecimal;
import com.acn.wildlifeextractor.domain.field.ExtractedInteger;
import com.acn.wildlifeextractor.domain.field.ExtractedReference;
import com.acn.wildlifeextractor.domain.field.ExtractedString;

/**
 * Extraction fields for the Competitors and Predators form
 * (schema {@code competitors-and-predators-v1}).
 */
public record CompetitorsAndPredatorsExtractionFields(
        ExtractedDecimal latitude,
        ExtractedDecimal longitude,
        ExtractedString comment,
        ExtractedDate observedTime,
        ExtractedReference species,
        ExtractedCode subPopulation,
        ExtractedBoolean isBirdAround,
        ExtractedCode presence,
        ExtractedReference competitorSpecies,
        ExtractedCode location,
        ExtractedCode behaviour,
        ExtractedCode currentNestStage,
        ExtractedCode outcome,
        ExtractedString nestReference,
        ExtractedString nestSiteName,
        ExtractedInteger numOfCompetitor,
        ExtractedCodeList impact,
        ExtractedCodeList action,
        ExtractedDecimal distanceFromNest,
        ExtractedBoolean hasCompetitorInfo,
        ExtractedCode speciesEcho,
        ExtractedBoolean breedingAttempt
) {
}
