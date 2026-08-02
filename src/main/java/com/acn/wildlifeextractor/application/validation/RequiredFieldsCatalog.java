package com.acn.wildlifeextractor.application.validation;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import org.springframework.stereotype.Component;

/**
 * The minimal set of fields that must be present for each form. A wildlife observation is not
 * actionable without at least its subject species, so {@code species} is required across all
 * forms. This is a conservative starting policy; teams can extend the required set per form as
 * data-collection standards firm up.
 */
@Component
public class RequiredFieldsCatalog {

    private final Map<WildlifeFormType, Set<String>> required = new EnumMap<>(WildlifeFormType.class);

    public RequiredFieldsCatalog() {
        for (WildlifeFormType type : WildlifeFormType.values()) {
            required.put(type, Set.of("species"));
        }
    }

    public Set<String> requiredFields(WildlifeFormType formType) {
        return required.getOrDefault(formType, Set.of());
    }
}
