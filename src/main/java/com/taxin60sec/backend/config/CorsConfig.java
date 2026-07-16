package com.taxin60sec.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    private final ApplicationProperties properties;

    public CorsConfig(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                ApplicationProperties.Cors cors = properties.cors();
                registry.addMapping("/**")
                        .allowedOrigins(cors.allowedOrigins().toArray(String[]::new))
                        .allowedMethods(cors.allowedMethods().toArray(String[]::new))
                        .allowedHeaders(cors.allowedHeaders().toArray(String[]::new));
            }
        };
    }
}
