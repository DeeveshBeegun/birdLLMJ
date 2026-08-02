package com.acn.wildlifeextractor.domain.reference;

/**
 * Resolves a spoken reference value deterministically against a trusted source. The reference type
 * identifies the category (for example {@code SPECIES}) so the resolution stage can dispatch.
 */
public interface ReferenceResolver {

    String referenceType();

    ReferenceResolution resolve(String lookupKey);
}
