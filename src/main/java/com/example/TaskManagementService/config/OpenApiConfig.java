package com.example.TaskManagementService.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Task Management Service API")
                        .version("1.0.0")
                        .description("""
                                A production-style backend system built with Spring Boot 3.
                                
                                Features:
                                - JWT Authentication & Authorization
                                - Project & Task Management
                                - Pagination & Filtering
                                - Redis Caching
                                - Scheduled Jobs
                                - WebSocket Real-time Notifications
                                - Testcontainers Integration
                                """)
                        .contact(new Contact()
                                .name("Chinmay Vijapure")
                                .email("vijapurechinmay@gmail.com")
                                .url("https://github.com/chinmay-vijapure-05"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                )

                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))

                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token in format: Bearer <token>")
                        )
                )

                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Local Development Server"))

                .externalDocs(new ExternalDocumentation()
                        .description("GitHub Repository")
                        .url("https://github.com/chinmay-vijapure-05/TaskManagerService"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("task-management-api")
                .pathsToMatch("/api/**")
                .build();
    }
}
