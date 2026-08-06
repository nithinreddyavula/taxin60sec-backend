package com.taxin60sec.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI tax60OpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tax60 API")
                        .version("v1")
                        .description("Backend foundation APIs for the Tax60 Chartered Accountant service platform.")
                        .license(new License().name("Private")));
    }
}
