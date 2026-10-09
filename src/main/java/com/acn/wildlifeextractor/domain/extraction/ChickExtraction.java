package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.enums.ChickCondition;
import com.acn.wildlifeextractor.domain.enums.ChickOptions;
import com.acn.wildlifeextractor.domain.enums.ChickStatus;
import com.acn.wildlifeextractor.domain.enums.IndividualNumber;
import com.acn.wildlifeextractor.domain.enums.SampleCollectedChick;
import com.acn.wildlifeextractor.domain.field.ExtractedBoolean;
import com.acn.wildlifeextractor.domain.field.ExtractedCode;
import com.acn.wildlifeextractor.domain.field.ExtractedCodeList;
import com.acn.wildlifeextractor.domain.field.ExtractedDate;
import com.acn.wildlifeextractor.domain.field.ExtractedDecimal;
import com.acn.wildlifeextractor.domain.field.ExtractedInteger;
import com.acn.wildlifeextractor.domain.field.ExtractedString;

/**
 * A single chick entry nested inside the Nest/Eggs/Chicks form. Never carries a database id,
 * timestamps, or a parent reference. A genuinely absent chick is simply not present in the list.
 *
 * @param legacy optional legacy section; {@code null} unless legacy fields are enabled
 */
public record ChickExtraction(
        ExtractedCode<IndividualNumber> individualNumber,
        ExtractedBoolean photoMentioned,
        ExtractedString comments,
        ExtractedDate hatchDate,
        ExtractedDecimal weight,
        ExtractedInteger age,
        ExtractedCode<ChickOptions> active,
        ExtractedCode<ChickOptions> hydrated,
        ExtractedCode<ChickStatus> status,
        ExtractedString chickId,
        ExtractedCode<ChickCondition> condition,
        ExtractedCodeList<SampleCollectedChick> sampleCollected,
        ChickExtractionLegacy legacy
) {
}
