package com.agendaclientes.api.usuario.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrarRequest(
        @NotBlank(message = "Nome é obrigatório") @Schema(example = "Maria Silva") String nome,
        @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") @Schema(example = "maria.silva@email.com") String email,
        @NotBlank(message = "Senha é obrigatória") @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres") @Schema(example = "minhaSenha123") String senha) {
}
