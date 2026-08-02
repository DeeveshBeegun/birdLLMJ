package com.acn.wildlifeextractor.infrastructure.reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.acn.wildlifeextractor.domain.reference.SpeciesReference;
import com.acn.wildlifeextractor.domain.reference.SpeciesReferenceResolver;
import org.springframework.stereotype.Component;

@Component
public class InMemorySpeciesReferenceResolver extends AbstractInMemoryReferenceResolver
        implements SpeciesReferenceResolver {

    public InMemorySpeciesReferenceResolver(ReferenceDataLoader loader) {
        super(toEntries(loader.load("reference/species.yml", SpeciesReference.class)));
    }

    private static List<LookupEntry> toEntries(List<SpeciesReference> species) {
        return species.stream()
                .map(s -> entry(
                        s.externalId(),
                        s.commonName() != null ? s.commonName() : s.speciesKey(),
                        Arrays.asList(s.speciesKey(), s.commonName(), s.latinName()),
                        List.of(),
                        Collections.singletonMap("categoryName", s.categoryName())))
                .toList();
    }

    @Override
    public String referenceType() {
        return "SPECIES";
    }
}
