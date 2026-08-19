package com.agendaclientes.api.cliente.infrastructure.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClienteRequest(
        @NotBlank(message = "Nome é obrigatório") String nome,
        @Email(message = "Email inválido") String email,
        @NotBlank(message = "Telefone é obrigatório") String telefone,
        String observacoes) {
}
