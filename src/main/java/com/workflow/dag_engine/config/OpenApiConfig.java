package com.workflow.dag_engine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dagEngineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DAG Engine & Workflow Nexus API")
                        .description("REST API documentation for DAG Engine, User Management, and Workflow Graph Services")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("DAG Engine Team")
                                .email("support@dag-engine.workflow.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }

}
