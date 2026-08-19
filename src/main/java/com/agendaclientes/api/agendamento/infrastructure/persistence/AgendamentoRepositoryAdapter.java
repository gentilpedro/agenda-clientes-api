package com.agendaclientes.api.agendamento.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.agendaclientes.api.agendamento.domain.Agendamento;
import com.agendaclientes.api.agendamento.domain.AgendamentoRepository;

@Repository
class AgendamentoRepositoryAdapter implements AgendamentoRepository {

    private final SpringDataAgendamentoRepository jpaRepository;

    AgendamentoRepositoryAdapter(SpringDataAgendamentoRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Agendamento save(Agendamento agendamento) {
        UUID id = agendamento.getId() != null ? agendamento.getId() : UUID.randomUUID();
        AgendamentoJpaEntity entity = new AgendamentoJpaEntity(
                id,
                agendamento.getUsuarioId(),
                agendamento.getClienteId(),
                agendamento.getDataHora(),
                agendamento.getDuracaoMinutos(),
                agendamento.getStatus(),
                agendamento.getObservacoes());
        AgendamentoJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Agendamento> findByIdAndUsuarioId(UUID id, UUID usuarioId) {
        return jpaRepository.findByIdAndUsuarioId(id, usuarioId).map(AgendamentoRepositoryAdapter::toDomain);
    }

    @Override
    public List<Agendamento> findAllByUsuarioId(UUID usuarioId) {
        return jpaRepository.findAllByUsuarioId(usuarioId).stream()
                .map(AgendamentoRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Agendamento> findByUsuarioIdAndPeriodo(UUID usuarioId, Instant inicio, Instant fimExclusivo) {
        return jpaRepository
                .findByUsuarioIdAndDataHoraGreaterThanEqualAndDataHoraLessThan(usuarioId, inicio, fimExclusivo)
                .stream()
                .map(AgendamentoRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByIdAndUsuarioId(UUID id, UUID usuarioId) {
        return jpaRepository.existsByIdAndUsuarioId(id, usuarioId);
    }

    private static Agendamento toDomain(AgendamentoJpaEntity entity) {
        return Agendamento.existente(
                entity.getId(),
                entity.getUsuarioId(),
                entity.getClienteId(),
                entity.getDataHora(),
                entity.getDuracaoMinutos(),
                entity.getStatus(),
                entity.getObservacoes());
    }
}
