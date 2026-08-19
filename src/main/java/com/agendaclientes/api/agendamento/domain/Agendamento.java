package com.agendaclientes.api.agendamento.domain;

import java.time.Instant;
import java.util.UUID;

public class Agendamento {

    private final UUID id;
    private final UUID usuarioId;
    private final UUID clienteId;
    private Instant dataHora;
    private Integer duracaoMinutos;
    private AgendamentoStatus status;
    private String observacoes;

    private Agendamento(UUID id, UUID usuarioId, UUID clienteId, Instant dataHora, Integer duracaoMinutos,
            AgendamentoStatus status, String observacoes) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.clienteId = clienteId;
        this.dataHora = dataHora;
        this.duracaoMinutos = duracaoMinutos;
        this.status = status;
        this.observacoes = observacoes;
    }

    public static Agendamento novo(UUID usuarioId, UUID clienteId, Instant dataHora, Integer duracaoMinutos,
            String observacoes) {
        return new Agendamento(null, usuarioId, clienteId, dataHora, duracaoMinutos, AgendamentoStatus.AGENDADO,
                observacoes);
    }

    public static Agendamento existente(UUID id, UUID usuarioId, UUID clienteId, Instant dataHora,
            Integer duracaoMinutos, AgendamentoStatus status, String observacoes) {
        return new Agendamento(id, usuarioId, clienteId, dataHora, duracaoMinutos, status, observacoes);
    }

    public void atualizarDados(Instant dataHora, Integer duracaoMinutos, String observacoes) {
        this.dataHora = dataHora;
        this.duracaoMinutos = duracaoMinutos;
        this.observacoes = observacoes;
    }

    public void cancelar() {
        if (status == AgendamentoStatus.CONCLUIDO) {
            throw new IllegalStateException("Não é possível cancelar um agendamento já concluído");
        }
        this.status = AgendamentoStatus.CANCELADO;
    }

    public void concluir() {
        if (status == AgendamentoStatus.CANCELADO) {
            throw new IllegalStateException("Não é possível concluir um agendamento cancelado");
        }
        this.status = AgendamentoStatus.CONCLUIDO;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public Instant getDataHora() {
        return dataHora;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public AgendamentoStatus getStatus() {
        return status;
    }

    public String getObservacoes() {
        return observacoes;
    }
}
