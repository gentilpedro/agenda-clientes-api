package com.agendaclientes.api.usuario.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EsqueciSenhaRequest(
        @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") @Schema(example = "maria.silva@email.com") String email) {
}
