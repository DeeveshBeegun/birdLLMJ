package com.acn.wildlifeextractor.infrastructure.reference;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.acn.wildlifeextractor.domain.reference.BirdReference;
import com.acn.wildlifeextractor.domain.reference.BirdReferenceResolver;
import org.springframework.stereotype.Component;

@Component
public class InMemoryBirdReferenceResolver extends AbstractInMemoryReferenceResolver
        implements BirdReferenceResolver {

    public InMemoryBirdReferenceResolver(ReferenceDataLoader loader) {
        super(toEntries(loader.load("reference/birds.yml", BirdReference.class)));
    }

    private static List<LookupEntry> toEntries(List<BirdReference> birds) {
        return birds.stream()
                .map(b -> {
                    Map<String, String> attributes = new LinkedHashMap<>();
                    attributes.put("sex", b.sex());
                    attributes.put("status", b.status());
                    return entry(
                            b.externalId(),
                            b.birdName() != null ? b.birdName() : b.birdID(),
                            Arrays.asList(b.birdID(), b.birdName(), b.leftRingNumber(), b.rightRingNumber(),
                                    b.comboLeft(), b.comboRight()),
                            List.of(),
                            attributes);
                })
                .toList();
    }

    @Override
    public String referenceType() {
        return "BIRD_OR_RINGING_RECORD";
    }
}
