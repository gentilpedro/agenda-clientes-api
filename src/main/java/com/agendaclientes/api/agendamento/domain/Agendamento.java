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
    private boolean confirmado;
    private Instant lembreteEnviadoEm;

    private Agendamento(UUID id, UUID usuarioId, UUID clienteId, Instant dataHora, Integer duracaoMinutos,
            AgendamentoStatus status, String observacoes, boolean confirmado, Instant lembreteEnviadoEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.clienteId = clienteId;
        this.dataHora = dataHora;
        this.duracaoMinutos = duracaoMinutos;
        this.status = status;
        this.observacoes = observacoes;
        this.confirmado = confirmado;
        this.lembreteEnviadoEm = lembreteEnviadoEm;
    }

    public static Agendamento novo(UUID usuarioId, UUID clienteId, Instant dataHora, Integer duracaoMinutos,
            String observacoes) {
        return new Agendamento(null, usuarioId, clienteId, dataHora, duracaoMinutos, AgendamentoStatus.AGENDADO,
                observacoes, false, null);
    }

    public static Agendamento existente(UUID id, UUID usuarioId, UUID clienteId, Instant dataHora,
            Integer duracaoMinutos, AgendamentoStatus status, String observacoes, boolean confirmado,
            Instant lembreteEnviadoEm) {
        return new Agendamento(id, usuarioId, clienteId, dataHora, duracaoMinutos, status, observacoes, confirmado,
                lembreteEnviadoEm);
    }

    /** Sobrecarga de conveniência pra código/testes anteriores à confirmação por WhatsApp. */
    public static Agendamento existente(UUID id, UUID usuarioId, UUID clienteId, Instant dataHora,
            Integer duracaoMinutos, AgendamentoStatus status, String observacoes) {
        return existente(id, usuarioId, clienteId, dataHora, duracaoMinutos, status, observacoes, false, null);
    }

    public void atualizarDados(Instant dataHora, Integer duracaoMinutos, String observacoes) {
        this.dataHora = dataHora;
        this.duracaoMinutos = duracaoMinutos;
        this.observacoes = observacoes;
        this.confirmado = false;
        this.lembreteEnviadoEm = null;
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

    public void confirmar() {
        if (status != AgendamentoStatus.AGENDADO) {
            throw new IllegalStateException("Só é possível confirmar um agendamento agendado");
        }
        this.confirmado = true;
    }

    public void marcarLembreteEnviado(Instant quando) {
        if (status != AgendamentoStatus.AGENDADO) {
            throw new IllegalStateException("Só é possível enviar lembrete para um agendamento agendado");
        }
        this.lembreteEnviadoEm = quando;
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

    public boolean isConfirmado() {
        return confirmado;
    }

    public Instant getLembreteEnviadoEm() {
        return lembreteEnviadoEm;
    }
}
