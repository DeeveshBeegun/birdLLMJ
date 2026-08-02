package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.field.ExtractedBoolean;
import com.acn.wildlifeextractor.domain.field.ExtractedCode;
import com.acn.wildlifeextractor.domain.field.ExtractedDate;
import com.acn.wildlifeextractor.domain.field.ExtractedInteger;
import com.acn.wildlifeextractor.domain.field.ExtractedReference;
import com.acn.wildlifeextractor.domain.field.ExtractedString;

/**
 * Legacy Ringing and Morphometrics fields, disabled by default and never required. Capture and
 * rearing flags are only meaningful when the legacy section is enabled.
 */
public record RingingMorphsLegacy(
        ExtractedReference maleParentId,
        ExtractedReference femaleParentId,
        ExtractedDate estimatedHatchDate,
        ExtractedInteger yearFledge,
        ExtractedCode capturePurpose,
        ExtractedCode nestSiteLaid,
        ExtractedCode rearType,
        ExtractedCode status,
        ExtractedBoolean hasCaptureInformation,
        ExtractedBoolean captiveBredToRearingInfo,
        ExtractedBoolean hasBasicMorphometrics,
        ExtractedBoolean hasAdditionalMorphometrics,
        ExtractedInteger exposedCulmen,
        ExtractedBoolean hasRearingInformation,
        ExtractedBoolean isHandReared,
        ExtractedBoolean isTranslocated,
        ExtractedInteger numOfFieldWorkers,
        ExtractedCode stage,
        ExtractedString reference,
        ExtractedReference nestSite,
        ExtractedString leftRingNumber,
        ExtractedString rightRingNumber
) {
}
