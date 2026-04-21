package ru.abovousqueadmala.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(AppProperties appProperties) {
        AppProperties.Docs docs = appProperties.docs();

        return new OpenAPI().info(new Info()
                .title(docs.title())
                .description(docs.description())
                .version(docs.version()));
    }
}
