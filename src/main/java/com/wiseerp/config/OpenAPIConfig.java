package com.wiseerp.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WiseERP Metadata API")
                        .description("APIs to manage metadata-driven entities and fields")
                        .version("0.1.0")
                        .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation().description("WiseERP Docs"));
    }
}
