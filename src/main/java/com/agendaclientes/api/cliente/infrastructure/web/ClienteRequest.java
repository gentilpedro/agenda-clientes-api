package com.agendaclientes.api.cliente.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClienteRequest(
        @NotBlank(message = "Nome é obrigatório") @Schema(example = "Maria Silva") String nome,
        @Email(message = "Email inválido") @Schema(example = "maria.silva@email.com") String email,
        @NotBlank(message = "Telefone é obrigatório") @Schema(example = "(11) 99999-0000") String telefone,
        @Schema(example = "Prefere atendimento à tarde") String observacoes) {
}
