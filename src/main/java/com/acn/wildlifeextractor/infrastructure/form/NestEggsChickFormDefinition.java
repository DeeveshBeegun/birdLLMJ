package com.acn.wildlifeextractor.infrastructure.form;

import com.acn.wildlifeextractor.domain.extraction.NestEggsChickExtractionFields;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class NestEggsChickFormDefinition
        extends AbstractClasspathFormDefinition<NestEggsChickExtractionFields> {

    public NestEggsChickFormDefinition(ObjectMapper objectMapper) {
        super(WildlifeFormType.NEST_EGGS_CHICK, "nest-eggs-chick-v1", "nest-eggs-chick-prompt-v1",
                NestEggsChickExtractionFields.class, objectMapper);
    }
}
