package com.agendaclientes.api.agendamento.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AgendamentoRequest(
        @NotNull(message = "Cliente é obrigatório") UUID clienteId,
        @NotNull(message = "Data/hora é obrigatória") Instant dataHora,
        @NotNull(message = "Duração é obrigatória") @Min(value = 5, message = "Duração mínima é 5 minutos") Integer duracaoMinutos,
        String observacoes) {
}
