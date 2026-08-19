package com.agendaclientes.api.agendamento.infrastructure.web;

import org.springframework.stereotype.Component;

import com.agendaclientes.api.agendamento.domain.Agendamento;

@Component
class AgendamentoWebMapper {

    AgendamentoResponse toResponse(Agendamento agendamento) {
        return new AgendamentoResponse(
                agendamento.getId(),
                agendamento.getClienteId(),
                agendamento.getDataHora(),
                agendamento.getDuracaoMinutos(),
                agendamento.getStatus(),
                agendamento.getObservacoes());
    }
}
