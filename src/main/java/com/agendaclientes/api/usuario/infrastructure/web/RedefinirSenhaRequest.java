package com.agendaclientes.api.usuario.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(
        @NotBlank(message = "Email é obrigatório") String email,
        @NotBlank(message = "Token é obrigatório") String token,
        @NotBlank(message = "Nova senha é obrigatória") @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres") String novaSenha) {
}
