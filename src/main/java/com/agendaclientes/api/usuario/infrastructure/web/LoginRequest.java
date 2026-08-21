package com.agendaclientes.api.usuario.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email é obrigatório") @Schema(example = "maria.silva@email.com") String email,
        @NotBlank(message = "Senha é obrigatória") @Schema(example = "minhaSenha123") String senha) {
}
