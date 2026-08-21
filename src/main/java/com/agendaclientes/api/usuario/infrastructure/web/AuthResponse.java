package com.agendaclientes.api.usuario.infrastructure.web;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(description = "JWT a ser enviado como `Authorization: Bearer <token>`") String token,
        UUID id,
        String nome,
        String email) {
}
