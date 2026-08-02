package com.acn.wildlifeextractor.infrastructure.form;

import com.acn.wildlifeextractor.domain.extraction.NestSiteCharacteristicsExtractionFields;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class NestSiteCharacteristicsFormDefinition
        extends AbstractClasspathFormDefinition<NestSiteCharacteristicsExtractionFields> {

    public NestSiteCharacteristicsFormDefinition(ObjectMapper objectMapper) {
        super(WildlifeFormType.NEST_SITE_CHARACTERISTICS, "nest-site-characteristics-v1",
                "nest-site-characteristics-prompt-v1",
                NestSiteCharacteristicsExtractionFields.class, objectMapper);
    }
}
