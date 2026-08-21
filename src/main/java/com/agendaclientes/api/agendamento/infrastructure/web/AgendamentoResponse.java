package com.agendaclientes.api.agendamento.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

import com.agendaclientes.api.agendamento.domain.AgendamentoStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record AgendamentoResponse(
        @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id,
        @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID clienteId,
        @Schema(example = "2026-08-20T14:30:00Z") Instant dataHora,
        @Schema(example = "60") Integer duracaoMinutos,
        AgendamentoStatus status,
        @Schema(example = "Primeira consulta") String observacoes) {
}
