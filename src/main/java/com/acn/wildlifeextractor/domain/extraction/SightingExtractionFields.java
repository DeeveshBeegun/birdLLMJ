package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.enums.Behaviour;
import com.acn.wildlifeextractor.domain.enums.BirdSex;
import com.acn.wildlifeextractor.domain.enums.CauseOfDeath;
import com.acn.wildlifeextractor.domain.enums.FeederLocation;
import com.acn.wildlifeextractor.domain.enums.Ring;
import com.acn.wildlifeextractor.domain.enums.SightingType;
import com.acn.wildlifeextractor.domain.enums.Stage;
import com.acn.wildlifeextractor.domain.enums.StageOfDecomposition;
import com.acn.wildlifeextractor.domain.enums.SubPopulation;
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
        ExtractedCode<SubPopulation> subPopulation,
        ExtractedCode<SightingType> sightingType,
        ExtractedCode<FeederLocation> feederLocation,
        ExtractedReference species,
        ExtractedString nestSite,
        ExtractedCode<Ring> upperRight,
        ExtractedCode<Ring> upperLeft,
        ExtractedCode<Ring> lowerRight,
        ExtractedCode<Ring> lowerLeft,
        ExtractedCode<Ring> leftLeg,
        ExtractedCode<Ring> rightLeg,
        ExtractedCode<BirdSex> seenSex,
        ExtractedCode<Stage> stage,
        ExtractedCode<CauseOfDeath> causeOfDeath,
        ExtractedCode<StageOfDecomposition> stageOfDecomposition,
        ExtractedCodeList<Behaviour> behaviour,
        ExtractedInteger suspectedPairNumber,
        ExtractedReference ringingMorphs,
        PhysicalConditionExtraction physicalCondition,
        ExtractedBoolean photoMentioned
) {
}
