package com.agendaclientes.api.usuario.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.agendaclientes.api.shared.security.TokenCipher;
import com.agendaclientes.api.usuario.domain.Usuario;
import com.agendaclientes.api.usuario.domain.UsuarioRepository;

@Repository
class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final SpringDataUsuarioRepository jpaRepository;
    private final TokenCipher tokenCipher;

    UsuarioRepositoryAdapter(SpringDataUsuarioRepository jpaRepository, TokenCipher tokenCipher) {
        this.jpaRepository = jpaRepository;
        this.tokenCipher = tokenCipher;
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
                usuario.getResetTokenExpiraEm(),
                usuario.getWhatsappWabaId(),
                usuario.getWhatsappPhoneNumberId(),
                usuario.getWhatsappNumeroExibicao(),
                tokenCipher.encrypt(usuario.getWhatsappTokenAcesso()),
                usuario.getWhatsappConectadoEm(),
                usuario.getWhatsappTemplateStatus());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Usuario> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public Optional<Usuario> findByWhatsappPhoneNumberId(String phoneNumberId) {
        return jpaRepository.findByWhatsappPhoneNumberId(phoneNumberId).map(this::toDomain);
    }

    private Usuario toDomain(UsuarioJpaEntity entity) {
        return Usuario.existente(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getSenhaHash(),
                entity.getCriadoEm(),
                entity.getResetTokenHash(),
                entity.getResetTokenExpiraEm(),
                entity.getWhatsappWabaId(),
                entity.getWhatsappPhoneNumberId(),
                entity.getWhatsappNumeroExibicao(),
                tokenCipher.decrypt(entity.getWhatsappTokenAcessoCriptografado()),
                entity.getWhatsappConectadoEm(),
                entity.getWhatsappTemplateStatus());
    }
}
