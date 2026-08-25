package com.agendaclientes.api.agendamento.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AgendamentoTest {

    private final UUID usuarioId = UUID.randomUUID();
    private final UUID clienteId = UUID.randomUUID();

    private Agendamento agendado() {
        return Agendamento.existente(UUID.randomUUID(), usuarioId, clienteId, Instant.now(), 30,
                AgendamentoStatus.AGENDADO, null, false, null);
    }

    @Test
    void confirmar_deveMarcarConfirmadoQuandoAgendado() {
        Agendamento agendamento = agendado();

        agendamento.confirmar();

        assertThat(agendamento.isConfirmado()).isTrue();
    }

    @Test
    void confirmar_deveLancarExcecaoQuandoNaoAgendado() {
        Agendamento cancelado = Agendamento.existente(UUID.randomUUID(), usuarioId, clienteId, Instant.now(), 30,
                AgendamentoStatus.CANCELADO, null, false, null);

        assertThatThrownBy(cancelado::confirmar).isInstanceOf(IllegalStateException.class);
        assertThat(cancelado.isConfirmado()).isFalse();
    }

    @Test
    void marcarLembreteEnviado_deveGuardarInstanteQuandoAgendado() {
        Agendamento agendamento = agendado();
        Instant agora = Instant.now();

        agendamento.marcarLembreteEnviado(agora);

        assertThat(agendamento.getLembreteEnviadoEm()).isEqualTo(agora);
    }

    @Test
    void marcarLembreteEnviado_deveLancarExcecaoQuandoNaoAgendado() {
        Agendamento concluido = Agendamento.existente(UUID.randomUUID(), usuarioId, clienteId, Instant.now(), 30,
                AgendamentoStatus.CONCLUIDO, null, false, null);

        assertThatThrownBy(() -> concluido.marcarLembreteEnviado(Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void atualizarDados_deveResetarConfirmacaoELembrete() {
        Agendamento agendamento = Agendamento.existente(UUID.randomUUID(), usuarioId, clienteId, Instant.now(), 30,
                AgendamentoStatus.AGENDADO, null, true, Instant.now());

        agendamento.atualizarDados(Instant.now().plusSeconds(3600), 60, "novo horário");

        assertThat(agendamento.isConfirmado()).isFalse();
        assertThat(agendamento.getLembreteEnviadoEm()).isNull();
    }

    @Test
    void cancelar_aindaDeveFuncionarComoAntesQuandoNaoConcluido() {
        Agendamento agendamento = agendado();

        agendamento.cancelar();

        assertThat(agendamento.getStatus()).isEqualTo(AgendamentoStatus.CANCELADO);
    }
}
