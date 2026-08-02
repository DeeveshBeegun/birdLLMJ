package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.field.ExtractedCode;
import com.acn.wildlifeextractor.domain.field.ExtractedInteger;
import com.acn.wildlifeextractor.domain.field.ExtractedString;

/**
 * Legacy Nest-Site Characteristics fields, disabled by default and never required.
 */
public record NestSiteCharacteristicsLegacy(
        ExtractedInteger diameter,
        ExtractedString maleId,
        ExtractedString femaleId,
        ExtractedInteger numOfFieldWorkers,
        ExtractedCode treeStatus,
        ExtractedCode treeSpeciesCode,
        ExtractedCode malePresence,
        ExtractedCode femalePresence
) {
}
