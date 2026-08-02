package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.field.ExtractedDate;

/**
 * Legacy egg fields, disabled by default. Present in the model only so the schema can be
 * extended once legacy support is explicitly enabled; never required.
 */
public record EggExtractionLegacy(
        ExtractedDate dateLaying,
        ExtractedDate dateChickAge
) {
}
