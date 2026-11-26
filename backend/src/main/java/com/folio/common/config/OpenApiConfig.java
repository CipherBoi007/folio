package com.folio.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI at {@code /swagger-ui.html} with an "Authorize" button for the bearer token. */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    OpenAPI folioOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Folio API")
                        .version("v1")
                        .description("Library operations console. Every copy, accounted for. "
                                + "Sign in via POST /api/v1/auth/login and paste the token into Authorize."))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
