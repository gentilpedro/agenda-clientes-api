package com.agendaclientes.api.cliente.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository {

    Cliente save(Cliente cliente);

    Optional<Cliente> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    List<Cliente> findAllByUsuarioId(UUID usuarioId);

    void deleteById(UUID id);

    boolean existsByIdAndUsuarioId(UUID id, UUID usuarioId);
}
