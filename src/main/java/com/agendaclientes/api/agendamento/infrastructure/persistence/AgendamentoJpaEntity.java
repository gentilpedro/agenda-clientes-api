package com.agendaclientes.api.agendamento.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import com.agendaclientes.api.agendamento.domain.AgendamentoStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "agendamentos")
public class AgendamentoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgendamentoStatus status;

    private String observacoes;

    protected AgendamentoJpaEntity() {
    }

    public AgendamentoJpaEntity(UUID id, UUID usuarioId, UUID clienteId, Instant dataHora, Integer duracaoMinutos,
            AgendamentoStatus status, String observacoes) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.clienteId = clienteId;
        this.dataHora = dataHora;
        this.duracaoMinutos = duracaoMinutos;
        this.status = status;
        this.observacoes = observacoes;
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
