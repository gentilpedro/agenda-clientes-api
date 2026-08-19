package com.agendaclientes.api.cliente.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String nome,
        String email,
        String telefone,
        String observacoes,
        Instant criadoEm) {
}
