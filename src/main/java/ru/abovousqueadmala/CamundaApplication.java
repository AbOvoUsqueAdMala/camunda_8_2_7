package ru.abovousqueadmala;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@Deployment(resources = {"classpath*:/bpmn/**/*.bpmn", "classpath*:/dmn/**/*.dmn"})
@OpenAPIDefinition(info = @Info(
        title = "${springdoc.info.title}",
        description = "${springdoc.info.description}",
        version = "${springdoc.info.version}"
))
public class CamundaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamundaApplication.class, args);
    }
}
