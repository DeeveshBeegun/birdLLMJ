package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.field.ExtractedCode;
import com.acn.wildlifeextractor.domain.field.ExtractedDate;
import com.acn.wildlifeextractor.domain.field.ExtractedDecimal;
import com.acn.wildlifeextractor.domain.field.ExtractedReference;
import com.acn.wildlifeextractor.domain.field.ExtractedString;
import com.acn.wildlifeextractor.domain.field.ExtractedTime;

/**
 * Extraction fields for the Nest-Site Characteristics form
 * (schema {@code nest-site-characteristics-v1}).
 *
 * <p>{@code bird1Id}/{@code bird2Id} are the current bird-identity fields and must not be
 * conflated with the legacy {@code maleId}/{@code femaleId}.</p>
 *
 * @param legacy optional legacy section; {@code null} unless legacy fields are enabled
 */
public record NestSiteCharacteristicsExtractionFields(
        ExtractedDecimal latitude,
        ExtractedDecimal longitude,
        ExtractedString comment,
        ExtractedDate observedTime,
        ExtractedTime startTime,
        ExtractedTime endTime,
        ExtractedReference species,
        ExtractedCode subPopulation,
        ExtractedString bird1Id,
        ExtractedString bird1Sex,
        ExtractedString bird2Id,
        ExtractedString bird2Sex,
        ExtractedCode context,
        ExtractedString nestSiteName,
        ExtractedString nestReference,
        ExtractedCode nestType,
        ExtractedReference treeSpecies,
        ExtractedCode nestStageFound,
        ExtractedString nestHeight,
        ExtractedString proximityToTrunk,
        ExtractedString canopyHeight,
        ExtractedString treeCircumference,
        ExtractedDecimal bearing,
        ExtractedCode sideOfTree,
        ExtractedDecimal entranceDiameter,
        ExtractedDecimal cavityDepth,
        ExtractedDecimal heightAboveNestBowl,
        ExtractedString treeHeight,
        ExtractedCode foliageDensity,
        ExtractedCode positionInCanopy,
        ExtractedCode habitatType,
        NestSiteCharacteristicsLegacy legacy
) {
}
