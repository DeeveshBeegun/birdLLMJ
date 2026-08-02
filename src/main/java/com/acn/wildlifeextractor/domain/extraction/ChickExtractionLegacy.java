package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.field.ExtractedDate;
import com.acn.wildlifeextractor.domain.field.ExtractedString;

/**
 * Legacy chick fields, disabled by default. Present in the model only so the schema can be
 * extended once legacy support is explicitly enabled; never required.
 */
public record ChickExtractionLegacy(
        ExtractedDate dateLaying,
        ExtractedDate dateChickAge,
        ExtractedString ringID
) {
}
