package com.acn.wildlifeextractor.infrastructure.reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.acn.wildlifeextractor.domain.reference.TreeSpeciesReference;
import com.acn.wildlifeextractor.domain.reference.TreeSpeciesReferenceResolver;
import org.springframework.stereotype.Component;

@Component
public class InMemoryTreeSpeciesReferenceResolver extends AbstractInMemoryReferenceResolver
        implements TreeSpeciesReferenceResolver {

    public InMemoryTreeSpeciesReferenceResolver(ReferenceDataLoader loader) {
        super(toEntries(loader.load("reference/tree-species.yml", TreeSpeciesReference.class)));
    }

    private static List<LookupEntry> toEntries(List<TreeSpeciesReference> trees) {
        return trees.stream()
                .map(t -> entry(
                        t.externalId(),
                        t.scientificName() != null ? t.scientificName() : t.code(),
                        Arrays.asList(t.code(), t.scientificName()),
                        List.of(),
                        Collections.singletonMap("status", t.status())))
                .toList();
    }

    @Override
    public String referenceType() {
        return "TREE_SPECIES";
    }
}
