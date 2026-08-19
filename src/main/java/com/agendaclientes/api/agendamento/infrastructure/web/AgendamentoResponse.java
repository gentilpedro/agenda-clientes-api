package com.agendaclientes.api.agendamento.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

import com.agendaclientes.api.agendamento.domain.AgendamentoStatus;

public record AgendamentoResponse(
        UUID id,
        UUID clienteId,
        Instant dataHora,
        Integer duracaoMinutos,
        AgendamentoStatus status,
        String observacoes) {
}
