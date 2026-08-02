package com.acn.wildlifeextractor.domain.extraction;

import com.acn.wildlifeextractor.domain.field.ExtractedBoolean;
import com.acn.wildlifeextractor.domain.field.ExtractedCode;
import com.acn.wildlifeextractor.domain.field.ExtractedDate;
import com.acn.wildlifeextractor.domain.field.ExtractedString;

/**
 * A single egg entry nested inside the Nest/Eggs/Chicks form. Never carries a database id,
 * timestamps, or a parent reference. A genuinely absent egg is simply not present in the list.
 *
 * @param legacy optional legacy section; {@code null} unless legacy fields are enabled
 */
public record EggExtraction(
        ExtractedCode individualNumber,
        ExtractedBoolean photoMentioned,
        ExtractedString comments,
        ExtractedCode fertility,
        ExtractedCode status,
        ExtractedDate dateLaid,
        EggExtractionLegacy legacy
) {
}
