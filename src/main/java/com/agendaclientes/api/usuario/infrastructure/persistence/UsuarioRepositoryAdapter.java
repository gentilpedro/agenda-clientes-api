package com.agendaclientes.api.usuario.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.agendaclientes.api.usuario.domain.Usuario;
import com.agendaclientes.api.usuario.domain.UsuarioRepository;

@Repository
class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final SpringDataUsuarioRepository jpaRepository;

    UsuarioRepositoryAdapter(SpringDataUsuarioRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario save(Usuario usuario) {
        UUID id = usuario.getId() != null ? usuario.getId() : UUID.randomUUID();
        UsuarioJpaEntity entity = new UsuarioJpaEntity(
                id,
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getSenhaHash(),
                usuario.getCriadoEm(),
                usuario.getResetTokenHash(),
                usuario.getResetTokenExpiraEm());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Usuario> findById(UUID id) {
        return jpaRepository.findById(id).map(UsuarioRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(UsuarioRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    private static Usuario toDomain(UsuarioJpaEntity entity) {
        return Usuario.existente(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getSenhaHash(),
                entity.getCriadoEm(),
                entity.getResetTokenHash(),
                entity.getResetTokenExpiraEm());
    }
}
