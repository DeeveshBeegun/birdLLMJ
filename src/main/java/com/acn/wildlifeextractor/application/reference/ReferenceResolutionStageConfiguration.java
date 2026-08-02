package com.acn.wildlifeextractor.application.reference;

import com.acn.wildlifeextractor.application.validation.FieldStatusScanner.ScanResult;
import com.acn.wildlifeextractor.application.validation.ValidationFindings;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a no-op {@link ReferenceResolutionStage} fallback used until a deterministic resolver
 * is supplied. References are left exactly as the model marked them; any the model itself flagged
 * as unresolved are still recorded by the field scanner.
 */
@Configuration
public class ReferenceResolutionStageConfiguration {

    @Bean
    @ConditionalOnMissingBean(ReferenceResolutionStage.class)
    ReferenceResolutionStage noOpReferenceResolutionStage() {
        return new ReferenceResolutionStage() {
            @Override
            public void resolve(WildlifeFormType formType, JsonNode fields, ScanResult scan,
                                ValidationFindings findings) {
                // Intentionally no-op.
            }
        };
    }
}
