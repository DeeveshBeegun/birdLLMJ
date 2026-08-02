package com.acn.wildlifeextractor.infrastructure.reference;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.acn.wildlifeextractor.domain.reference.ReferenceResolution;
import com.acn.wildlifeextractor.domain.reference.ReferenceResolutionStatus;
import org.junit.jupiter.api.Test;

class ReferenceResolverTest {

    private final ReferenceDataLoader loader = new ReferenceDataLoader();
    private final InMemorySpeciesReferenceResolver species = new InMemorySpeciesReferenceResolver(loader);
    private final InMemoryTreeSpeciesReferenceResolver trees = new InMemoryTreeSpeciesReferenceResolver(loader);

    /** Resolver seeded with data that exercises alias and duplicate-exact-key paths. */
    static class SeededResolver extends AbstractInMemoryReferenceResolver {
        SeededResolver() {
            super(List.of(
                    entry("X1", "One", Arrays.asList("alpha"), Arrays.asList("first", "a-1"), Map.of()),
                    entry("X2", "Two", Arrays.asList("beta"), Arrays.asList("shared"), Map.of()),
                    entry("X3", "Three", Arrays.asList("shared"), List.of(), Map.of())));
        }

        @Override
        public String referenceType() {
            return "TEST";
        }
    }

    private final SeededResolver seeded = new SeededResolver();

    @Test
    void resolvesByExactName() {
        ReferenceResolution result = species.resolve("Echo Parakeet");
        assertThat(result.status()).isEqualTo(ReferenceResolutionStatus.RESOLVED);
        assertThat(result.resolvedExternalId()).isEqualTo("SP-ECHO");
    }

    @Test
    void resolvesByExactCode() {
        assertThat(species.resolve("ECHO_PARAKEET").resolvedExternalId()).isEqualTo("SP-ECHO");
    }

    @Test
    void resolvesByExactIdentifier() {
        assertThat(species.resolve("SP-PINK").resolvedExternalId()).isEqualTo("SP-PINK");
    }

    @Test
    void resolvesByLatinName() {
        assertThat(species.resolve("Nesoenas mayeri").resolvedExternalId()).isEqualTo("SP-PINK");
    }

    @Test
    void resolvesByAliasCaseInsensitively() {
        ReferenceResolution result = seeded.resolve("First");
        assertThat(result.status()).isEqualTo(ReferenceResolutionStatus.RESOLVED);
        assertThat(result.resolvedExternalId()).isEqualTo("X1");
    }

    @Test
    void returnsNotFoundForUnknownValue() {
        assertThat(species.resolve("Dodo").status()).isEqualTo(ReferenceResolutionStatus.NOT_FOUND);
    }

    @Test
    void returnsAmbiguousWhenMultipleExactMatches() {
        ReferenceResolution result = seeded.resolve("shared");
        assertThat(result.status()).isEqualTo(ReferenceResolutionStatus.AMBIGUOUS);
        assertThat(result.candidates()).hasSize(2);
    }

    @Test
    void returnsAmbiguousFuzzyCandidatesWithoutAutoSelecting() {
        ReferenceResolution result = species.resolve("parakeet");
        assertThat(result.status()).isEqualTo(ReferenceResolutionStatus.AMBIGUOUS);
        assertThat(result.candidates()).extracting("externalId").contains("SP-ECHO", "SP-RING");
        assertThat(result.resolvedExternalId()).isNull();
    }

    @Test
    void doesNotMatchAcrossReferenceTypes() {
        assertThat(trees.resolve("Echo Parakeet").status()).isEqualTo(ReferenceResolutionStatus.NOT_FOUND);
    }
}
