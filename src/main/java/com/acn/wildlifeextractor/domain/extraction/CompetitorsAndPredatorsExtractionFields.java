package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.enums.Action;
import com.acn.wildlifeextractor.domain.enums.CompetitorBehaviour;
import com.acn.wildlifeextractor.domain.enums.CompetitorOutcome;
import com.acn.wildlifeextractor.domain.enums.CompetitorsLocation;
import com.acn.wildlifeextractor.domain.enums.CurrentNestStage;
import com.acn.wildlifeextractor.domain.enums.Impact;
import com.acn.wildlifeextractor.domain.enums.Presence;
import com.acn.wildlifeextractor.domain.enums.SpeciesEcho;
import com.acn.wildlifeextractor.domain.enums.SubPopulation;
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
        ExtractedCode<SubPopulation> subPopulation,
        ExtractedBoolean isBirdAround,
        ExtractedCode<Presence> presence,
        ExtractedReference competitorSpecies,
        ExtractedCode<CompetitorsLocation> location,
        ExtractedCode<CompetitorBehaviour> behaviour,
        ExtractedCode<CurrentNestStage> currentNestStage,
        ExtractedCode<CompetitorOutcome> outcome,
        ExtractedString nestReference,
        ExtractedString nestSiteName,
        ExtractedInteger numOfCompetitor,
        ExtractedCodeList<Impact> impact,
        ExtractedCodeList<Action> action,
        ExtractedDecimal distanceFromNest,
        ExtractedBoolean hasCompetitorInfo,
        ExtractedCode<SpeciesEcho> speciesEcho,
        ExtractedBoolean breedingAttempt
) {
}
