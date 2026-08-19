package com.agendaclientes.api.cliente.domain;

import java.time.Instant;
import java.util.UUID;

public class Cliente {

    private final UUID id;
    private final UUID usuarioId;
    private String nome;
    private String email;
    private String telefone;
    private String observacoes;
    private final Instant criadoEm;

    private Cliente(UUID id, UUID usuarioId, String nome, String email, String telefone, String observacoes,
            Instant criadoEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.observacoes = observacoes;
        this.criadoEm = criadoEm;
    }

    public static Cliente novo(UUID usuarioId, String nome, String email, String telefone, String observacoes) {
        return new Cliente(null, usuarioId, nome, email, telefone, observacoes, Instant.now());
    }

    public static Cliente existente(UUID id, UUID usuarioId, String nome, String email, String telefone,
            String observacoes, Instant criadoEm) {
        return new Cliente(id, usuarioId, nome, email, telefone, observacoes, criadoEm);
    }

    public void atualizarDados(String nome, String email, String telefone, String observacoes) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.observacoes = observacoes;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
