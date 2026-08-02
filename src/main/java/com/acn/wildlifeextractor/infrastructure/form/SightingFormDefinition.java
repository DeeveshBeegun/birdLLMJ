package com.acn.wildlifeextractor.infrastructure.form;

import com.acn.wildlifeextractor.domain.extraction.SightingExtractionFields;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class SightingFormDefinition extends AbstractClasspathFormDefinition<SightingExtractionFields> {

    public SightingFormDefinition(ObjectMapper objectMapper) {
        super(WildlifeFormType.SIGHTING, "sighting-v1", "sighting-prompt-v1",
                SightingExtractionFields.class, objectMapper);
    }
}
