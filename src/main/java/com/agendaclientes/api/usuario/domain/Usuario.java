package com.agendaclientes.api.usuario.domain;

import java.time.Instant;
import java.util.UUID;

public class Usuario {

    private final UUID id;
    private String nome;
    private final String email;
    private String senhaHash;
    private final Instant criadoEm;
    private String resetTokenHash;
    private Instant resetTokenExpiraEm;

    private Usuario(UUID id, String nome, String email, String senhaHash, Instant criadoEm,
            String resetTokenHash, Instant resetTokenExpiraEm) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.criadoEm = criadoEm;
        this.resetTokenHash = resetTokenHash;
        this.resetTokenExpiraEm = resetTokenExpiraEm;
    }

    public static Usuario novo(String nome, String email, String senhaHash) {
        return new Usuario(null, nome, email, senhaHash, Instant.now(), null, null);
    }

    public static Usuario existente(UUID id, String nome, String email, String senhaHash, Instant criadoEm,
            String resetTokenHash, Instant resetTokenExpiraEm) {
        return new Usuario(id, nome, email, senhaHash, criadoEm, resetTokenHash, resetTokenExpiraEm);
    }

    public void definirTokenReset(String tokenHash, Instant expiraEm) {
        this.resetTokenHash = tokenHash;
        this.resetTokenExpiraEm = expiraEm;
    }

    public boolean tokenResetValido(String tokenHash) {
        return resetTokenHash != null
                && resetTokenHash.equals(tokenHash)
                && resetTokenExpiraEm != null
                && resetTokenExpiraEm.isAfter(Instant.now());
    }

    public void redefinirSenha(String novaSenhaHash) {
        this.senhaHash = novaSenhaHash;
        this.resetTokenHash = null;
        this.resetTokenExpiraEm = null;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public String getResetTokenHash() {
        return resetTokenHash;
    }

    public Instant getResetTokenExpiraEm() {
        return resetTokenExpiraEm;
    }
}
