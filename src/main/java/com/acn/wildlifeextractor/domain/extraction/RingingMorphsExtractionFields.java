package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.field.ExtractedCode;
import com.acn.wildlifeextractor.domain.field.ExtractedCodeList;
import com.acn.wildlifeextractor.domain.field.ExtractedDate;
import com.acn.wildlifeextractor.domain.field.ExtractedDecimal;
import com.acn.wildlifeextractor.domain.field.ExtractedInteger;
import com.acn.wildlifeextractor.domain.field.ExtractedReference;
import com.acn.wildlifeextractor.domain.field.ExtractedString;
import com.acn.wildlifeextractor.domain.field.ExtractedTime;

/**
 * Extraction fields for the Ringing and Morphometrics form (schema {@code ringing-morphs-v1}).
 *
 * <p>The {@code observer} and {@code ringer} user references remain unresolved until resolved
 * by trusted application context or a reference provider; the model never invents them.</p>
 *
 * @param legacy optional legacy section; {@code null} unless legacy fields are enabled
 */
public record RingingMorphsExtractionFields(
        ExtractedDecimal latitude,
        ExtractedDecimal longitude,
        ExtractedString comment,
        ExtractedDate releasedDate,
        ExtractedTime observedTime,
        ExtractedReference species,
        ExtractedCode subPopulation,
        ExtractedReference observer,
        ExtractedReference ringer,
        ExtractedTime startTime,
        ExtractedTime endTime,
        ExtractedString birdName,
        ExtractedString birdID,
        ExtractedCode lowerLeftRingNumber,
        ExtractedCode lowerRightRingNumber,
        ExtractedCode upperLeftRingNumber,
        ExtractedCode upperRightRingNumber,
        ExtractedCode leftLeg,
        ExtractedCode rightLeg,
        ExtractedCode birdSex,
        ExtractedCode birdStage,
        ExtractedInteger apparentAge,
        ExtractedCode ageType,
        ExtractedInteger weight,
        ExtractedInteger tail,
        ExtractedInteger tailBrush,
        ExtractedInteger tarsusLength,
        ExtractedInteger wing,
        ExtractedInteger p8,
        ExtractedInteger p8Brush,
        ExtractedInteger head,
        ExtractedInteger culmenToGape,
        ExtractedInteger culmenFeather,
        ExtractedInteger culmenToSkull,
        PhysicalConditionExtraction physicalCondition,
        ExtractedString nestSiteName,
        ExtractedString nestReference,
        ExtractedString idMale,
        ExtractedString idFemale,
        ExtractedCode rearingType,
        ExtractedCode whereLaid,
        ExtractedCode whereFledged,
        ExtractedCode whereReleased,
        ExtractedCode whereNow,
        ExtractedCode captureMethod,
        ExtractedCodeList sampleCollected,
        RingingMorphsLegacy legacy
) {
}
