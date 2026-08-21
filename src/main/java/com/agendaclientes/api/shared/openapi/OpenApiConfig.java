package com.agendaclientes.api.shared.openapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    /**
     * Nome do esquema de segurança referenciado pelos controllers protegidos
     * via {@code @SecurityRequirement}.
     */
    public static final String BEARER_AUTH = "bearerAuth";

    private final String versao;

    public OpenApiConfig(@Value("${app.version}") String versao) {
        this.versao = versao;
    }

    @Bean
    public OpenAPI agendaClientesOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Agenda Clientes API")
                        .description("""
                                API para gerenciamento de clientes e agendamentos.

                                Os endpoints de `/api/auth` são públicos. Os demais exigem um JWT: \
                                obtenha o token em `POST /api/auth/login` e informe-o no botão **Authorize**.""")
                        .version(versao))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT devolvido por POST /api/auth/login")));
    }
}
