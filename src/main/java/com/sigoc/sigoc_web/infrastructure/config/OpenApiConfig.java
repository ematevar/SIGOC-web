package com.sigoc.sigoc_web.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Contrato OpenAPI 3 de la API REST de SIGOC Web (/swagger-ui.html y /v3/api-docs).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sigocOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SIGOC Web API")
                        .version("3.0.0")
                        .description("API REST del Sistema Integrado de Gestión de Obras Civiles "
                                + "(Arquitectura Hexagonal, 25 casos de uso, 5 roles ISO/IEC 27001 A.5.3)."));
    }
}
