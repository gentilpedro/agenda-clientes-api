package com.agendaclientes.api.usuario.infrastructure.web;

import java.util.UUID;

public record AuthResponse(String token, UUID id, String nome, String email) {
}
