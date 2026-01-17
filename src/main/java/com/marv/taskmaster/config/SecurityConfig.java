package com.marv.taskmaster.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF (Common for REST APIs, as we don't use Cookies for auth)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Define Public vs Private Endpoints
                .authorizeHttpRequests(auth -> auth
                        // Allow Auth endpoints (Signup/Login)
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Allow Swagger UI & Open API Docs
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Lock everything else
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}