package com.agendaclientes.api.whatsapp.application;

import java.util.UUID;

import com.agendaclientes.api.usuario.domain.Usuario;

public interface WhatsappConnectionService {

    Usuario buscarStatus(UUID usuarioId);

    Usuario conectar(UUID usuarioId, String code, String wabaId, String phoneNumberId);

    void desconectar(UUID usuarioId);
}
