package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.enums.ActivityLevel;
import com.acn.wildlifeextractor.domain.enums.Beak;
import com.acn.wildlifeextractor.domain.enums.ClinicalSigns;
import com.acn.wildlifeextractor.domain.enums.Condition;
import com.acn.wildlifeextractor.domain.enums.Ears;
import com.acn.wildlifeextractor.domain.enums.Eyes;
import com.acn.wildlifeextractor.domain.enums.FeatherCondition;
import com.acn.wildlifeextractor.domain.enums.Gape;
import com.acn.wildlifeextractor.domain.enums.InjurySeverity;
import com.acn.wildlifeextractor.domain.enums.Legs;
import com.acn.wildlifeextractor.domain.enums.Nostrils;
import com.acn.wildlifeextractor.domain.enums.Parasite;
import com.acn.wildlifeextractor.domain.enums.Plucked;
import com.acn.wildlifeextractor.domain.enums.Plumage;
import com.acn.wildlifeextractor.domain.enums.Primaries;
import com.acn.wildlifeextractor.domain.enums.SuspectedDisease;
import com.acn.wildlifeextractor.domain.enums.Vent;
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
        ExtractedCode<Condition> condition,
        ExtractedCode<ActivityLevel> activityLevel,
        ExtractedCode<SuspectedDisease> suspectedDisease,
        ExtractedCodeList<ClinicalSigns> clinicalSigns,
        ExtractedCode<InjurySeverity> injurySeverity,
        ExtractedCode<FeatherCondition> featherCondition,
        ExtractedCode<Primaries> yellowFeathers,
        ExtractedCode<Primaries> primaries,
        ExtractedCode<Primaries> wingFeathers,
        ExtractedCode<Primaries> tailFeathers,
        ExtractedCode<Primaries> bodyFeathers,
        ExtractedCode<Beak> beak,
        ExtractedCode<Plumage> plumage,
        ExtractedCode<Legs> legs,
        ExtractedCode<Vent> vent,
        ExtractedCode<Gape> gape,
        ExtractedCode<Nostrils> nostrils,
        ExtractedCode<Ears> ears,
        ExtractedCode<Parasite> parasite,
        ExtractedCode<Plucked> plucked,
        ExtractedCode<Eyes> eyes
) {
}
