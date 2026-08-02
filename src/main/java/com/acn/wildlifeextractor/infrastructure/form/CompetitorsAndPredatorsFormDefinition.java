package com.acn.wildlifeextractor.infrastructure.form;

import com.acn.wildlifeextractor.domain.extraction.CompetitorsAndPredatorsExtractionFields;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class CompetitorsAndPredatorsFormDefinition
        extends AbstractClasspathFormDefinition<CompetitorsAndPredatorsExtractionFields> {

    public CompetitorsAndPredatorsFormDefinition(ObjectMapper objectMapper) {
        super(WildlifeFormType.COMPETITORS_AND_PREDATORS, "competitors-and-predators-v1",
                "competitors-and-predators-prompt-v1",
                CompetitorsAndPredatorsExtractionFields.class, objectMapper);
    }
}
