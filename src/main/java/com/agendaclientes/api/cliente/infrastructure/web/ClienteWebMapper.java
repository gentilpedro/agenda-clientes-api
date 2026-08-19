package com.agendaclientes.api.cliente.infrastructure.web;

import org.springframework.stereotype.Component;

import com.agendaclientes.api.cliente.domain.Cliente;

@Component
class ClienteWebMapper {

    ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getObservacoes(),
                cliente.getCriadoEm());
    }
}
