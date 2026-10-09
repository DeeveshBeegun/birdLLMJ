package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.enums.Context;
import com.acn.wildlifeextractor.domain.enums.FoliageDensity;
import com.acn.wildlifeextractor.domain.enums.HabitatType;
import com.acn.wildlifeextractor.domain.enums.NestStage;
import com.acn.wildlifeextractor.domain.enums.NestType;
import com.acn.wildlifeextractor.domain.enums.PositionInCanopy;
import com.acn.wildlifeextractor.domain.enums.SideOfTree;
import com.acn.wildlifeextractor.domain.enums.SubPopulation;
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
        ExtractedCode<SubPopulation> subPopulation,
        ExtractedString bird1Id,
        ExtractedString bird1Sex,
        ExtractedString bird2Id,
        ExtractedString bird2Sex,
        ExtractedCode<Context> context,
        ExtractedString nestSiteName,
        ExtractedString nestReference,
        ExtractedCode<NestType> nestType,
        ExtractedReference treeSpecies,
        ExtractedCode<NestStage> nestStageFound,
        ExtractedString nestHeight,
        ExtractedString proximityToTrunk,
        ExtractedString canopyHeight,
        ExtractedString treeCircumference,
        ExtractedDecimal bearing,
        ExtractedCode<SideOfTree> sideOfTree,
        ExtractedDecimal entranceDiameter,
        ExtractedDecimal cavityDepth,
        ExtractedDecimal heightAboveNestBowl,
        ExtractedString treeHeight,
        ExtractedCode<FoliageDensity> foliageDensity,
        ExtractedCode<PositionInCanopy> positionInCanopy,
        ExtractedCode<HabitatType> habitatType,
        NestSiteCharacteristicsLegacy legacy
) {
}
