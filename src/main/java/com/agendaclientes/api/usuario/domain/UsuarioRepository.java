package com.agendaclientes.api.usuario.domain;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {

    Usuario save(Usuario usuario);

    Optional<Usuario> findById(UUID id);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
