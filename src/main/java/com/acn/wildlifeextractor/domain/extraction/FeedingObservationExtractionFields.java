package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.field.ExtractedCode;
import com.acn.wildlifeextractor.domain.field.ExtractedDate;
import com.acn.wildlifeextractor.domain.field.ExtractedDecimal;
import com.acn.wildlifeextractor.domain.field.ExtractedReference;
import com.acn.wildlifeextractor.domain.field.ExtractedString;

/**
 * Extraction fields for the Feeding Observation form (schema {@code feeding-observation-v1}).
 *
 * <p>The property {@code feedingOutCome} preserves the historical spelling; it may be migrated
 * to {@code feedingOutcome} in a future schema version.</p>
 */
public record FeedingObservationExtractionFields(
        ExtractedDecimal latitude,
        ExtractedDecimal longitude,
        ExtractedString comment,
        ExtractedDate observedTime,
        ExtractedCode subPopulation,
        ExtractedReference species,
        ExtractedCode leftLeg,
        ExtractedCode rightLeg,
        ExtractedCode upperRight,
        ExtractedCode upperLeft,
        ExtractedCode lowerRight,
        ExtractedCode lowerLeft,
        ExtractedReference ringingMorphs,
        ExtractedCode stage,
        ExtractedCode seenSex,
        ExtractedCode itemConsumed,
        ExtractedCode itemDescription,
        ExtractedReference treeSpecies,
        ExtractedCode treeStatus,
        ExtractedString foragingHeight,
        ExtractedString treeHeight,
        ExtractedCode breedingStatus,
        ExtractedString nestReference,
        ExtractedString nestSiteName,
        ExtractedCode feedingOutCome,
        ExtractedCode cloud,
        ExtractedCode wind,
        ExtractedCode rain,
        ExtractedCode method
) {
}
