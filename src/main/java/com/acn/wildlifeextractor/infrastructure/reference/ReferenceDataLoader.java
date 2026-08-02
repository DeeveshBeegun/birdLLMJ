package com.acn.wildlifeextractor.infrastructure.reference;

import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Loads reference records from a YAML classpath resource with an {@code entries:} list. A missing
 * file yields an empty list so the application can start without any seeded reference data.
 */
@Component
public class ReferenceDataLoader {

    private final YAMLMapper yamlMapper = YAMLMapper.builder().build();

    public <T> List<T> load(String resourcePath, Class<T> type) {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            return List.of();
        }
        try (InputStream in = resource.getInputStream()) {
            JsonNode root = yamlMapper.readTree(in);
            if (root == null) {
                return List.of();
            }
            JsonNode entries = root.get("entries");
            if (entries == null || !entries.isArray()) {
                return List.of();
            }
            CollectionType listType = yamlMapper.getTypeFactory().constructCollectionType(List.class, type);
            return yamlMapper.convertValue(entries, listType);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load reference data from " + resourcePath, e);
        }
    }
}
