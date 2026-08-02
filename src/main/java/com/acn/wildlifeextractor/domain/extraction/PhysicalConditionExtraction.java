package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.field.ExtractedBoolean;
import com.acn.wildlifeextractor.domain.field.ExtractedCode;
import com.acn.wildlifeextractor.domain.field.ExtractedCodeList;

/**
 * Reusable physical-condition assessment shared by the Sighting and Ringing/Morphometrics
 * forms. Every value carries its own extraction status so a genuinely unobserved trait is
 * never represented by a placeholder.
 */
public record PhysicalConditionExtraction(
        ExtractedBoolean hasClinicalSigns,
        ExtractedBoolean hasInjury,
        ExtractedCode condition,
        ExtractedCode activityLevel,
        ExtractedCode suspectedDisease,
        ExtractedCodeList clinicalSigns,
        ExtractedCode injurySeverity,
        ExtractedCode featherCondition,
        ExtractedCode yellowFeathers,
        ExtractedCode primaries,
        ExtractedCode wingFeathers,
        ExtractedCode tailFeathers,
        ExtractedCode bodyFeathers,
        ExtractedCode beak,
        ExtractedCode plumage,
        ExtractedCode legs,
        ExtractedCode vent,
        ExtractedCode gape,
        ExtractedCode nostrils,
        ExtractedCode ears,
        ExtractedCode parasite,
        ExtractedCode plucked,
        ExtractedCode eyes
) {
}
