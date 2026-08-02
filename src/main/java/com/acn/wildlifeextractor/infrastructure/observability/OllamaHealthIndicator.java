package com.acn.wildlifeextractor.infrastructure.observability;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Reports reachability of the configured Ollama server. It performs a short, best-effort probe and
 * never blocks startup. This indicator contributes to the {@code health} endpoint but not to the
 * readiness probe, so a missing model server does not take the application out of rotation.
 */
@Component("ollama")
public class OllamaHealthIndicator implements HealthIndicator {

    private final String baseUrl;

    public OllamaHealthIndicator(Environment environment) {
        this.baseUrl = environment.getProperty("spring.ai.ollama.base-url", "http://localhost:11434");
    }

    @Override
    public Health health() {
        // The client is created lazily here so context startup never opens a socket.
        try {
            HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/version"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 200 && response.statusCode() < 500) {
                return Health.up().withDetail("baseUrl", baseUrl).build();
            }
            return Health.down().withDetail("baseUrl", baseUrl).withDetail("status", response.statusCode()).build();
        } catch (Exception e) {
            return Health.down().withDetail("baseUrl", baseUrl).withDetail("reason", "unreachable").build();
        }
    }
}
