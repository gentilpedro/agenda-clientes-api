package com.agendaclientes.api.agendamento.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgendamentoRepository {

    Agendamento save(Agendamento agendamento);

    Optional<Agendamento> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    List<Agendamento> findAllByUsuarioId(UUID usuarioId);

    List<Agendamento> findByUsuarioIdAndPeriodo(UUID usuarioId, Instant inicio, Instant fimExclusivo);

    void deleteById(UUID id);

    boolean existsByIdAndUsuarioId(UUID id, UUID usuarioId);
}
