package com.agendaclientes.api.whatsapp.infrastructure.web;

import org.springframework.stereotype.Component;

import com.agendaclientes.api.usuario.domain.Usuario;

@Component
class WhatsappWebMapper {

    WhatsappStatusResponse toResponse(Usuario usuario) {
        return new WhatsappStatusResponse(
                usuario.whatsappConectado(),
                usuario.getWhatsappNumeroExibicao(),
                usuario.getWhatsappConectadoEm(),
                usuario.getWhatsappTemplateStatus());
    }
}
