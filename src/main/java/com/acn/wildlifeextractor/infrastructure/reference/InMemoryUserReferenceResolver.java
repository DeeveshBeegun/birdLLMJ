package com.acn.wildlifeextractor.infrastructure.reference;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.acn.wildlifeextractor.domain.reference.UserReference;
import com.acn.wildlifeextractor.domain.reference.UserReferenceResolver;
import org.springframework.stereotype.Component;

@Component
public class InMemoryUserReferenceResolver extends AbstractInMemoryReferenceResolver
        implements UserReferenceResolver {

    public InMemoryUserReferenceResolver(ReferenceDataLoader loader) {
        super(toEntries(loader.load("reference/users.yml", UserReference.class)));
    }

    private static List<LookupEntry> toEntries(List<UserReference> users) {
        return users.stream()
                .map(u -> entry(u.externalId(), u.name(), Arrays.asList(u.name()), List.of(), Map.of()))
                .toList();
    }

    @Override
    public String referenceType() {
        return "USER";
    }
}
