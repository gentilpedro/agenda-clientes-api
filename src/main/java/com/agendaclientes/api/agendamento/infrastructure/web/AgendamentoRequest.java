package com.agendaclientes.api.agendamento.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AgendamentoRequest(
        @NotNull(message = "Cliente é obrigatório") UUID clienteId,

        @Schema(description = "Início do agendamento, em UTC (ISO-8601)", example = "2026-08-20T14:30:00Z")
        @NotNull(message = "Data/hora é obrigatória") Instant dataHora,

        @Schema(example = "60")
        @NotNull(message = "Duração é obrigatória") @Min(value = 5, message = "Duração mínima é 5 minutos") Integer duracaoMinutos,

        String observacoes) {
}
