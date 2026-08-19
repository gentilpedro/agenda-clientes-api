package com.agendaclientes.api.usuario.infrastructure.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EsqueciSenhaRequest(
        @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") String email) {
}
