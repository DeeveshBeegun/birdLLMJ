package com.acn.wildlifeextractor.infrastructure.form;

import com.acn.wildlifeextractor.domain.extraction.RingingMorphsExtractionFields;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class RingingMorphsFormDefinition
        extends AbstractClasspathFormDefinition<RingingMorphsExtractionFields> {

    public RingingMorphsFormDefinition(ObjectMapper objectMapper) {
        super(WildlifeFormType.RINGING_MORPHS, "ringing-morphs-v1", "ringing-morphs-prompt-v1",
                RingingMorphsExtractionFields.class, objectMapper);
    }
}
