package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.field.ExtractedBoolean;
import com.acn.wildlifeextractor.domain.field.ExtractedCode;
import com.acn.wildlifeextractor.domain.field.ExtractedCodeList;
import com.acn.wildlifeextractor.domain.field.ExtractedDate;
import com.acn.wildlifeextractor.domain.field.ExtractedInteger;
import com.acn.wildlifeextractor.domain.field.ExtractedReference;

/**
 * Legacy Nest, Eggs and Chicks fields, disabled by default and never required.
 */
public record NestEggsChickLegacy(
        ExtractedInteger numOfFieldWorkers,
        ExtractedBoolean hasNestStage,
        ExtractedBoolean hasPairInformation,
        ExtractedDate dateOutcome,
        ExtractedBoolean hasEggChick,
        ExtractedReference femaleSighting,
        ExtractedReference maleSighting,
        ExtractedCode femaleSeen,
        ExtractedCode maleSeen,
        ExtractedCode failureReason,
        ExtractedCodeList management,
        ExtractedCode sampleCollectedChick,
        ExtractedCode sampleCollectedEgg,
        ExtractedCode individualNumber,
        ExtractedReference nestSiteCharacteristics
) {
}
