package com.acn.wildlifeextractor.infrastructure.reference;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.acn.wildlifeextractor.domain.reference.NestReference;
import com.acn.wildlifeextractor.domain.reference.NestReferenceResolver;
import org.springframework.stereotype.Component;

@Component
public class InMemoryNestReferenceResolver extends AbstractInMemoryReferenceResolver
        implements NestReferenceResolver {

    public InMemoryNestReferenceResolver(ReferenceDataLoader loader) {
        super(toEntries(loader.load("reference/nests.yml", NestReference.class)));
    }

    private static List<LookupEntry> toEntries(List<NestReference> nests) {
        return nests.stream()
                .map(n -> entry(
                        n.externalId(),
                        n.nestSiteName() != null ? n.nestSiteName() : n.nestReference(),
                        Arrays.asList(n.nestReference(), n.nestSiteName()),
                        List.of(),
                        Map.of()))
                .toList();
    }

    @Override
    public String referenceType() {
        return "NEST_SITE";
    }
}
