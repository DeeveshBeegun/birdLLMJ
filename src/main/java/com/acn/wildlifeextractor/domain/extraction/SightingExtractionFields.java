package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.field.ExtractedBoolean;
import com.acn.wildlifeextractor.domain.field.ExtractedCode;
import com.acn.wildlifeextractor.domain.field.ExtractedCodeList;
import com.acn.wildlifeextractor.domain.field.ExtractedDecimal;
import com.acn.wildlifeextractor.domain.field.ExtractedInteger;
import com.acn.wildlifeextractor.domain.field.ExtractedReference;
import com.acn.wildlifeextractor.domain.field.ExtractedString;
import com.acn.wildlifeextractor.domain.field.ExtractedTime;

/**
 * Extraction fields for the Sighting form (schema {@code sighting-v1}).
 */
public record SightingExtractionFields(
        ExtractedDecimal latitude,
        ExtractedDecimal longitude,
        ExtractedString comment,
        ExtractedTime observedTime,
        ExtractedTime startTime,
        ExtractedTime endTime,
        ExtractedCode subPopulation,
        ExtractedCode sightingType,
        ExtractedCode feederLocation,
        ExtractedReference species,
        ExtractedString nestSite,
        ExtractedCode upperRight,
        ExtractedCode upperLeft,
        ExtractedCode lowerRight,
        ExtractedCode lowerLeft,
        ExtractedCode leftLeg,
        ExtractedCode rightLeg,
        ExtractedCode seenSex,
        ExtractedCode stage,
        ExtractedCode causeOfDeath,
        ExtractedCode stageOfDecomposition,
        ExtractedCodeList behaviour,
        ExtractedInteger suspectedPairNumber,
        ExtractedReference ringingMorphs,
        PhysicalConditionExtraction physicalCondition,
        ExtractedBoolean photoMentioned
) {
}
