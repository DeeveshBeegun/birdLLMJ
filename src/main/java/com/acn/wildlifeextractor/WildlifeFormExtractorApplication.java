package com.acn.wildlifeextractor;

import com.acn.wildlifeextractor.configuration.ExtractionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the wildlife form extraction service.
 *
 * <p>The application context must start without a running Ollama server. All provider
 * interaction is deferred until an extraction request is actually processed.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = ExtractionProperties.class)
public class WildlifeFormExtractorApplication {

    public static void main(String[] args) {
        SpringApplication.run(WildlifeFormExtractorApplication.class, args);
    }
}
