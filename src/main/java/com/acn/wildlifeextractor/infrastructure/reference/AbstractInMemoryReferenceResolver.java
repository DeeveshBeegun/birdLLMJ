package com.acn.wildlifeextractor.infrastructure.reference;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.acn.wildlifeextractor.domain.field.ReferenceCandidate;
import com.acn.wildlifeextractor.domain.reference.ReferenceResolution;
import com.acn.wildlifeextractor.domain.reference.ReferenceResolver;

/**
 * In-memory reference resolver implementing the mandated resolution order: exact identifier, exact
 * code, exact name, deterministic alias, then a fuzzy candidate search. A unique exact match
 * resolves; multiple exact matches or any fuzzy matches yield an ambiguous result with candidates
 * (never auto-selected); no match at all is not-found.
 */
public abstract class AbstractInMemoryReferenceResolver implements ReferenceResolver {

    /** A single indexed reference entry. */
    protected record LookupEntry(String externalId, String displayName,
                                 List<String> exactKeys, List<String> aliases,
                                 Map<String, String> safeAttributes) {
    }

    private final List<LookupEntry> entries;

    protected AbstractInMemoryReferenceResolver(List<LookupEntry> entries) {
        this.entries = List.copyOf(entries);
    }

    @Override
    public ReferenceResolution resolve(String lookupKey) {
        if (lookupKey == null || lookupKey.isBlank()) {
            return ReferenceResolution.notFound();
        }
        String normalized = normalize(lookupKey);
        List<LookupEntry> exact = entries.stream()
                .filter(entry -> matchesExact(entry, normalized, lookupKey))
                .toList();
        if (exact.size() == 1) {
            LookupEntry match = exact.get(0);
            return ReferenceResolution.resolved(match.externalId(), match.displayName());
        }
        if (exact.size() > 1) {
            return ReferenceResolution.ambiguous(toCandidates(exact, 1.0));
        }
        List<LookupEntry> fuzzy = entries.stream()
                .filter(entry -> matchesFuzzy(entry, normalized))
                .toList();
        if (fuzzy.isEmpty()) {
            return ReferenceResolution.notFound();
        }
        return ReferenceResolution.ambiguous(toCandidates(fuzzy, 0.5));
    }

    private boolean matchesExact(LookupEntry entry, String normalized, String rawLookup) {
        if (entry.externalId() != null && entry.externalId().equalsIgnoreCase(rawLookup.strip())) {
            return true;
        }
        for (String key : entry.exactKeys()) {
            if (normalize(key).equals(normalized)) {
                return true;
            }
        }
        for (String alias : entry.aliases()) {
            if (normalize(alias).equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesFuzzy(LookupEntry entry, String normalized) {
        if (normalized.length() < 3) {
            return false;
        }
        List<String> haystacks = new ArrayList<>(entry.exactKeys());
        haystacks.addAll(entry.aliases());
        for (String candidate : haystacks) {
            String normalizedCandidate = normalize(candidate);
            if (normalizedCandidate.length() >= 3
                    && (normalizedCandidate.contains(normalized) || normalized.contains(normalizedCandidate))) {
                return true;
            }
        }
        return false;
    }

    private List<ReferenceCandidate> toCandidates(List<LookupEntry> matches, double score) {
        return matches.stream()
                .map(entry -> new ReferenceCandidate(entry.externalId(), entry.displayName(), score, entry.safeAttributes()))
                .toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Builds a {@link LookupEntry}, filtering out null/blank keys and null attribute values. */
    protected static LookupEntry entry(String externalId, String displayName,
                                       List<String> exactKeys, List<String> aliases,
                                       Map<String, String> safeAttributes) {
        List<String> keys = new ArrayList<>();
        for (String key : exactKeys) {
            if (key != null && !key.isBlank()) {
                keys.add(key);
            }
        }
        List<String> cleanAliases = new ArrayList<>();
        if (aliases != null) {
            for (String alias : aliases) {
                if (alias != null && !alias.isBlank()) {
                    cleanAliases.add(alias);
                }
            }
        }
        Map<String, String> attributes = new LinkedHashMap<>();
        if (safeAttributes != null) {
            safeAttributes.forEach((k, v) -> {
                if (k != null && v != null) {
                    attributes.put(k, v);
                }
            });
        }
        return new LookupEntry(externalId, displayName, List.copyOf(keys), List.copyOf(cleanAliases), Map.copyOf(attributes));
    }
}
