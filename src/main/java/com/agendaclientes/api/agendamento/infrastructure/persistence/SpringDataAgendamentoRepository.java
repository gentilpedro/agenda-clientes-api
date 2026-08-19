package com.agendaclientes.api.agendamento.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAgendamentoRepository extends JpaRepository<AgendamentoJpaEntity, UUID> {

    Optional<AgendamentoJpaEntity> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    List<AgendamentoJpaEntity> findAllByUsuarioId(UUID usuarioId);

    boolean existsByIdAndUsuarioId(UUID id, UUID usuarioId);

    List<AgendamentoJpaEntity> findByUsuarioIdAndDataHoraGreaterThanEqualAndDataHoraLessThan(
            UUID usuarioId, Instant inicio, Instant fimExclusivo);
}
