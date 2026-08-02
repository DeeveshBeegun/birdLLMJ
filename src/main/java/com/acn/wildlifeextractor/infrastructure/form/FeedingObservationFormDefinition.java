package com.acn.wildlifeextractor.infrastructure.form;

import com.acn.wildlifeextractor.domain.extraction.FeedingObservationExtractionFields;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class FeedingObservationFormDefinition
        extends AbstractClasspathFormDefinition<FeedingObservationExtractionFields> {

    public FeedingObservationFormDefinition(ObjectMapper objectMapper) {
        super(WildlifeFormType.FEEDING_OBSERVATION, "feeding-observation-v1", "feeding-observation-prompt-v1",
                FeedingObservationExtractionFields.class, objectMapper);
    }
}
