package com.agendaclientes.api.cliente.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataClienteRepository extends JpaRepository<ClienteJpaEntity, UUID> {

    Optional<ClienteJpaEntity> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    List<ClienteJpaEntity> findAllByUsuarioId(UUID usuarioId);

    boolean existsByIdAndUsuarioId(UUID id, UUID usuarioId);
}
