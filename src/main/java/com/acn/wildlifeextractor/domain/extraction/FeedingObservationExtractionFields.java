package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.enums.BirdSexForFeedingObservation;
import com.acn.wildlifeextractor.domain.enums.BreedingStatus;
import com.acn.wildlifeextractor.domain.enums.Cloud;
import com.acn.wildlifeextractor.domain.enums.FeedingOutcome;
import com.acn.wildlifeextractor.domain.enums.ItemConsumed;
import com.acn.wildlifeextractor.domain.enums.ItemDescription;
import com.acn.wildlifeextractor.domain.enums.Method;
import com.acn.wildlifeextractor.domain.enums.Rain;
import com.acn.wildlifeextractor.domain.enums.Ring;
import com.acn.wildlifeextractor.domain.enums.Stage;
import com.acn.wildlifeextractor.domain.enums.SubPopulation;
import com.acn.wildlifeextractor.domain.enums.TreeStatus;
import com.acn.wildlifeextractor.domain.enums.Wind;
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
        ExtractedCode<SubPopulation> subPopulation,
        ExtractedReference species,
        ExtractedCode<Ring> leftLeg,
        ExtractedCode<Ring> rightLeg,
        ExtractedCode<Ring> upperRight,
        ExtractedCode<Ring> upperLeft,
        ExtractedCode<Ring> lowerRight,
        ExtractedCode<Ring> lowerLeft,
        ExtractedReference ringingMorphs,
        ExtractedCode<Stage> stage,
        ExtractedCode<BirdSexForFeedingObservation> seenSex,
        ExtractedCode<ItemConsumed> itemConsumed,
        ExtractedCode<ItemDescription> itemDescription,
        ExtractedReference treeSpecies,
        ExtractedCode<TreeStatus> treeStatus,
        ExtractedString foragingHeight,
        ExtractedString treeHeight,
        ExtractedCode<BreedingStatus> breedingStatus,
        ExtractedString nestReference,
        ExtractedString nestSiteName,
        ExtractedCode<FeedingOutcome> feedingOutCome,
        ExtractedCode<Cloud> cloud,
        ExtractedCode<Wind> wind,
        ExtractedCode<Rain> rain,
        ExtractedCode<Method> method
) {
}
