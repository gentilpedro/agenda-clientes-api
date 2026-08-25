package com.agendaclientes.api.whatsapp.infrastructure.web;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

public record WhatsappStatusResponse(
        boolean conectado,
        @Schema(example = "+55 11 99999-0000") String numeroExibicao,
        Instant conectadoEm,
        @Schema(description = "null quando não conectado", example = "APROVADO") String templateStatus) {
}
