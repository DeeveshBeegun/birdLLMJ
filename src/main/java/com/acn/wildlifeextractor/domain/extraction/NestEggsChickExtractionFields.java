package com.acn.wildlifeextractor.domain.extraction;

import java.util.List;

import com.acn.wildlifeextractor.domain.enums.BirdSex;
import com.acn.wildlifeextractor.domain.enums.ClutchNumber;
import com.acn.wildlifeextractor.domain.enums.CurrentNestStage;
import com.acn.wildlifeextractor.domain.enums.GenderPresence;
import com.acn.wildlifeextractor.domain.enums.NestOutcome;
import com.acn.wildlifeextractor.domain.enums.NestSiteAccessed;
import com.acn.wildlifeextractor.domain.enums.ObservationType;
import com.acn.wildlifeextractor.domain.enums.PurposeOfVisit;
import com.acn.wildlifeextractor.domain.enums.SubPopulation;
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
        ExtractedCode<SubPopulation> subPopulation,
        ExtractedString nestReference,
        ExtractedString nestSiteName,
        ExtractedCode<PurposeOfVisit> purposeOfVisit,
        ExtractedCode<NestSiteAccessed> nestSiteAccessed,
        ExtractedCode<CurrentNestStage> currentNestStage,
        ExtractedCode<GenderPresence> malePresence,
        ExtractedString maleId,
        ExtractedCode<GenderPresence> femalePresence,
        ExtractedString femaleId,
        ExtractedString bird1ID,
        ExtractedCode<BirdSex> bird1Sex,
        ExtractedString bird2ID,
        ExtractedCode<BirdSex> bird2Sex,
        ExtractedCode<ObservationType> observationType,
        ExtractedCode<ClutchNumber> clutchNumber,
        List<EggExtraction> eggDetails,
        List<ChickExtraction> chickDetails,
        ExtractedCode<NestOutcome> nestOutcome,
        ExtractedBoolean photoMentioned,
        NestEggsChickLegacy legacy
) {
    public NestEggsChickExtractionFields {
        eggDetails = eggDetails == null ? List.of() : List.copyOf(eggDetails);
        chickDetails = chickDetails == null ? List.of() : List.copyOf(chickDetails);
    }
}
