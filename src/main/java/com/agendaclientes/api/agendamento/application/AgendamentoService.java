package com.agendaclientes.api.agendamento.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.agendaclientes.api.agendamento.domain.Agendamento;

public interface AgendamentoService {

    Agendamento criar(UUID usuarioId, UUID clienteId, Instant dataHora, Integer duracaoMinutos, String observacoes);

    Agendamento atualizar(UUID usuarioId, UUID id, Instant dataHora, Integer duracaoMinutos, String observacoes);

    Agendamento buscarPorId(UUID usuarioId, UUID id);

    List<Agendamento> listarTodos(UUID usuarioId);

    List<Agendamento> listarPorData(UUID usuarioId, LocalDate data);

    Agendamento cancelar(UUID usuarioId, UUID id);

    Agendamento concluir(UUID usuarioId, UUID id);

    void remover(UUID usuarioId, UUID id);
}
