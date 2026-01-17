package com.marv.taskmaster.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        // 1. Define the Security Scheme (The "Authorize" button)
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .name("Bearer Authentication")
                .scheme("bearer")
                .bearerFormat("JWT");

        // 2. Add the Security Requirement (Apply it globally)
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        // 3. Define Info (Title, Version, Description)
        Info info = new Info()
                .title("Taskmaster API")
                .version("1.0")
                .contact(new Contact().name("Marv").email("rokemarvellous@gmail.com"))
                .description("Taskmaster API with Spring Boot 3 and Oracle DB");

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(new Components().addSecuritySchemes("Bearer Authentication", securityScheme));
    }
}