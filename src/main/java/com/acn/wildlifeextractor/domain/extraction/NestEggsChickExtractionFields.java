package com.acn.wildlifeextractor.domain.extraction;

import java.util.List;

import com.acn.wildlifeextractor.domain.field.ExtractedBoolean;
import com.acn.wildlifeextractor.domain.field.ExtractedCode;
import com.acn.wildlifeextractor.domain.field.ExtractedDate;
import com.acn.wildlifeextractor.domain.field.ExtractedDecimal;
import com.acn.wildlifeextractor.domain.field.ExtractedReference;
import com.acn.wildlifeextractor.domain.field.ExtractedString;
import com.acn.wildlifeextractor.domain.field.ExtractedTime;

/**
 * Extraction fields for the Nest, Eggs and Chicks form (schema {@code nest-eggs-chick-v1}).
 *
 * <p>Nested egg and chick entries never carry generated identifiers, and a genuinely absent
 * clutch or brood is represented by an empty list rather than a placeholder entry.</p>
 *
 * @param legacy optional legacy section; {@code null} unless legacy fields are enabled
 */
public record NestEggsChickExtractionFields(
        ExtractedDecimal latitude,
        ExtractedDecimal longitude,
        ExtractedString comment,
        ExtractedDate observedTime,
        ExtractedTime startTime,
        ExtractedTime endTime,
        ExtractedReference species,
        ExtractedCode subPopulation,
        ExtractedString nestReference,
        ExtractedString nestSiteName,
        ExtractedCode purposeOfVisit,
        ExtractedCode nestSiteAccessed,
        ExtractedCode currentNestStage,
        ExtractedCode malePresence,
        ExtractedString maleId,
        ExtractedCode femalePresence,
        ExtractedString femaleId,
        ExtractedString bird1ID,
        ExtractedCode bird1Sex,
        ExtractedString bird2ID,
        ExtractedCode bird2Sex,
        ExtractedCode observationType,
        ExtractedCode clutchNumber,
        List<EggExtraction> eggDetails,
        List<ChickExtraction> chickDetails,
        ExtractedCode nestOutcome,
        ExtractedBoolean photoMentioned,
        NestEggsChickLegacy legacy
) {
    public NestEggsChickExtractionFields {
        eggDetails = eggDetails == null ? List.of() : List.copyOf(eggDetails);
        chickDetails = chickDetails == null ? List.of() : List.copyOf(chickDetails);
    }
}
