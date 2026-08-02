package com.acn.wildlifeextractor.infrastructure.reference;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.acn.wildlifeextractor.domain.reference.CompetitorSpeciesReference;
import com.acn.wildlifeextractor.domain.reference.CompetitorSpeciesReferenceResolver;
import org.springframework.stereotype.Component;

@Component
public class InMemoryCompetitorSpeciesReferenceResolver extends AbstractInMemoryReferenceResolver
        implements CompetitorSpeciesReferenceResolver {

    public InMemoryCompetitorSpeciesReferenceResolver(ReferenceDataLoader loader) {
        super(toEntries(loader.load("reference/competitor-species.yml", CompetitorSpeciesReference.class)));
    }

    private static List<LookupEntry> toEntries(List<CompetitorSpeciesReference> competitors) {
        return competitors.stream()
                .map(c -> entry(c.externalId(), c.name(), Arrays.asList(c.name()), List.of(), Map.of()))
                .toList();
    }

    @Override
    public String referenceType() {
        return "COMPETITOR_SPECIES";
    }
}
