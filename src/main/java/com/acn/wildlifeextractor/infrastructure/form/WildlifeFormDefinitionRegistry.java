package com.acn.wildlifeextractor.infrastructure.form;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.acn.wildlifeextractor.domain.form.UnsupportedWildlifeFormException;
import com.acn.wildlifeextractor.domain.form.WildlifeFormDefinition;
import com.acn.wildlifeextractor.domain.form.WildlifeFormType;
import com.acn.wildlifeextractor.infrastructure.schema.CompiledSchemaCache;
import org.springframework.stereotype.Component;

/**
 * Immutable registry of all discovered {@link WildlifeFormDefinition} beans. At construction it
 * enforces uniqueness of form type and schema version and eagerly compiles every schema, so any
 * duplicate, missing or structurally invalid schema aborts application startup.
 */
@Component
public class WildlifeFormDefinitionRegistry {

    private final Map<WildlifeFormType, WildlifeFormDefinition<?>> byFormType;
    private final Map<String, WildlifeFormDefinition<?>> bySchemaVersion;

    public WildlifeFormDefinitionRegistry(List<WildlifeFormDefinition<?>> definitions,
                                          CompiledSchemaCache schemaCache) {
        Map<WildlifeFormType, WildlifeFormDefinition<?>> types = new EnumMap<>(WildlifeFormType.class);
        Map<String, WildlifeFormDefinition<?>> versions = new LinkedHashMap<>();

        for (WildlifeFormDefinition<?> definition : definitions) {
            if (definition.jsonSchema() == null) {
                throw new IllegalStateException("Missing schema for form " + definition.formType());
            }
            if (definition.systemPrompt() == null || definition.systemPrompt().isBlank()
                    || definition.formPrompt() == null || definition.formPrompt().isBlank()) {
                throw new IllegalStateException("Missing prompt for form " + definition.formType());
            }
            if (types.putIfAbsent(definition.formType(), definition) != null) {
                throw new IllegalStateException("Duplicate form type: " + definition.formType());
            }
            if (versions.putIfAbsent(definition.schemaVersion(), definition) != null) {
                throw new IllegalStateException("Duplicate schema version: " + definition.schemaVersion());
            }
            // Compile eagerly: an invalid schema fails startup here rather than at request time.
            schemaCache.getOrCompile(definition.jsonSchema());
        }

        this.byFormType = Map.copyOf(types);
        this.bySchemaVersion = Map.copyOf(versions);
    }

    public WildlifeFormDefinition<?> getByFormType(WildlifeFormType formType) {
        WildlifeFormDefinition<?> definition = byFormType.get(formType);
        if (definition == null) {
            throw new UnsupportedWildlifeFormException(formType);
        }
        return definition;
    }

    public Optional<WildlifeFormDefinition<?>> findByFormType(WildlifeFormType formType) {
        return Optional.ofNullable(byFormType.get(formType));
    }

    public Optional<WildlifeFormDefinition<?>> findBySchemaVersion(String schemaVersion) {
        return Optional.ofNullable(bySchemaVersion.get(schemaVersion));
    }

    public Collection<WildlifeFormDefinition<?>> all() {
        return byFormType.values();
    }

    public int size() {
        return byFormType.size();
    }
}
