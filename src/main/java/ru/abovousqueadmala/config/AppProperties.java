package ru.abovousqueadmala.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Camunda camunda,
        Elastic elastic,
        StubService stubService
) {

    public record Camunda(
            String jobIndex,
            Zeebe zeebe
    ) {
    }

    public record Zeebe(String processId) {
    }

    public record Elastic(String url) {
    }

    public record StubService(
            String baseUrl,
            String submitPath,
            String asyncSubmitPath
    ) {
    }
}
