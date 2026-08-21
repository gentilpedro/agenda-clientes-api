package com.agendaclientes.api.cliente.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record ClienteResponse(
        @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id,
        @Schema(example = "Maria Silva") String nome,
        @Schema(example = "maria.silva@email.com") String email,
        @Schema(example = "(11) 99999-0000") String telefone,
        @Schema(example = "Prefere atendimento à tarde") String observacoes,
        @Schema(example = "2026-08-15T13:00:00Z") Instant criadoEm) {
}
