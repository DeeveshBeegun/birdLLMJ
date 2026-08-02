package com.acn.wildlifeextractor.application.reference;

import com.acn.wildlifeextractor.application.validation.FieldStatusScanner.ScanResult;
import com.acn.wildlifeextractor.application.validation.ValidationFindings;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Pipeline stage that deterministically resolves spoken reference values (species, tree species,
 * birds, nests, users) against trusted sources, mutating the reference field objects in place and
 * recording any that remain unresolved. Runs after vocabulary validation and before business
 * validation.
 */
public interface ReferenceResolutionStage {

    void resolve(WildlifeFormType formType, JsonNode fields, ScanResult scan, ValidationFindings findings);
}
