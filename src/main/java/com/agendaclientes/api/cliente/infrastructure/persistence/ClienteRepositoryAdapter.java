package com.agendaclientes.api.cliente.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.agendaclientes.api.cliente.domain.Cliente;
import com.agendaclientes.api.cliente.domain.ClienteRepository;

@Repository
class ClienteRepositoryAdapter implements ClienteRepository {

    private final SpringDataClienteRepository jpaRepository;

    ClienteRepositoryAdapter(SpringDataClienteRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Cliente save(Cliente cliente) {
        UUID id = cliente.getId() != null ? cliente.getId() : UUID.randomUUID();
        ClienteJpaEntity entity = new ClienteJpaEntity(
                id,
                cliente.getUsuarioId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getObservacoes(),
                cliente.getCriadoEm());
        ClienteJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Cliente> findByIdAndUsuarioId(UUID id, UUID usuarioId) {
        return jpaRepository.findByIdAndUsuarioId(id, usuarioId).map(ClienteRepositoryAdapter::toDomain);
    }

    @Override
    public List<Cliente> findAllByUsuarioId(UUID usuarioId) {
        return jpaRepository.findAllByUsuarioId(usuarioId).stream()
                .map(ClienteRepositoryAdapter::toDomain)
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

    private static Cliente toDomain(ClienteJpaEntity entity) {
        return Cliente.existente(
                entity.getId(),
                entity.getUsuarioId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.getObservacoes(),
                entity.getCriadoEm());
    }
}
