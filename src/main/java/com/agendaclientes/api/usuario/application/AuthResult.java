package com.agendaclientes.api.usuario.application;

import com.agendaclientes.api.usuario.domain.Usuario;

public record AuthResult(String token, Usuario usuario) {
}
