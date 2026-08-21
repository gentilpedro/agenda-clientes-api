package com.agendaclientes.api.usuario.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(
        @NotBlank(message = "Email é obrigatório") @Schema(example = "maria.silva@email.com") String email,
        @NotBlank(message = "Token é obrigatório") @Schema(example = "8f14e45fceea167a5a36dedd4bea2543") String token,
        @NotBlank(message = "Nova senha é obrigatória") @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres") @Schema(example = "novaSenha456") String novaSenha) {
}
