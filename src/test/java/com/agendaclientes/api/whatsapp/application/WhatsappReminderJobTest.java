package com.agendaclientes.api.whatsapp.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.agendaclientes.api.agendamento.domain.Agendamento;
import com.agendaclientes.api.agendamento.domain.AgendamentoRepository;
import com.agendaclientes.api.agendamento.domain.AgendamentoStatus;
import com.agendaclientes.api.cliente.domain.Cliente;
import com.agendaclientes.api.cliente.domain.ClienteRepository;
import com.agendaclientes.api.usuario.domain.Usuario;
import com.agendaclientes.api.usuario.domain.UsuarioRepository;
import com.agendaclientes.api.whatsapp.infrastructure.graph.GraphApiClient;

@ExtendWith(MockitoExtension.class)
class WhatsappReminderJobTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private GraphApiClient graphApiClient;

    private WhatsappReminderJob job;

    private final UUID usuarioId = UUID.randomUUID();
    private final UUID clienteId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        job = new WhatsappReminderJob(agendamentoRepository, usuarioRepository, clienteRepository, graphApiClient,
                1440);
    }

    private Agendamento agendamentoElegivel() {
        return Agendamento.existente(UUID.randomUUID(), usuarioId, clienteId, Instant.now().plusSeconds(3600), 30,
                AgendamentoStatus.AGENDADO, null, false, null);
    }

    private Usuario usuarioConectadoETemplateAprovado() {
        Usuario usuario = Usuario.existente(usuarioId, "Consultório", "c@email.com", "hash", Instant.now(), null,
                null);
        usuario.conectarWhatsapp("waba-1", "phone-1", "+55 11 99999-0000", "token", Instant.now());
        usuario.atualizarStatusTemplate("APROVADO");
        return usuario;
    }

    private Cliente cliente() {
        return Cliente.existente(clienteId, usuarioId, "Cliente Teste", null, "+5511988888888", null, Instant.now());
    }

    @Test
    void enviarLembretes_deveEnviarEMarcarLembreteQuandoTudoElegivel() {
        Agendamento agendamento = agendamentoElegivel();
        when(agendamentoRepository.findElegiveisParaLembrete(any(), any())).thenReturn(List.of(agendamento));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioConectadoETemplateAprovado()));
        when(clienteRepository.findByIdAndUsuarioId(clienteId, usuarioId)).thenReturn(Optional.of(cliente()));

        job.enviarLembretes();

        verify(graphApiClient).enviarLembreteConfirmacao(eq("phone-1"), eq("token"), eq("+5511988888888"));
        ArgumentCaptor<Agendamento> captor = ArgumentCaptor.forClass(Agendamento.class);
        verify(agendamentoRepository).save(captor.capture());
        assertThat(captor.getValue().getLembreteEnviadoEm()).isNotNull();
    }

    @Test
    void enviarLembretes_naoDeveEnviarQuandoUsuarioNaoConectado() {
        Agendamento agendamento = agendamentoElegivel();
        when(agendamentoRepository.findElegiveisParaLembrete(any(), any())).thenReturn(List.of(agendamento));
        Usuario desconectado = Usuario.existente(usuarioId, "Consultório", "c@email.com", "hash", Instant.now(),
                null, null);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(desconectado));

        job.enviarLembretes();

        verify(graphApiClient, never()).enviarLembreteConfirmacao(anyString(), anyString(), anyString());
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void enviarLembretes_naoDeveEnviarQuandoTemplateAindaNaoAprovado() {
        Agendamento agendamento = agendamentoElegivel();
        when(agendamentoRepository.findElegiveisParaLembrete(any(), any())).thenReturn(List.of(agendamento));
        Usuario usuario = Usuario.existente(usuarioId, "Consultório", "c@email.com", "hash", Instant.now(), null,
                null);
        usuario.conectarWhatsapp("waba-1", "phone-1", "+55 11 99999-0000", "token", Instant.now());
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        job.enviarLembretes();

        verify(graphApiClient, never()).enviarLembreteConfirmacao(anyString(), anyString(), anyString());
    }

    @Test
    void enviarLembretes_naoDeveEnviarQuandoClienteNaoEncontrado() {
        Agendamento agendamento = agendamentoElegivel();
        when(agendamentoRepository.findElegiveisParaLembrete(any(), any())).thenReturn(List.of(agendamento));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioConectadoETemplateAprovado()));
        when(clienteRepository.findByIdAndUsuarioId(clienteId, usuarioId)).thenReturn(Optional.empty());

        job.enviarLembretes();

        verify(graphApiClient, never()).enviarLembreteConfirmacao(anyString(), anyString(), anyString());
    }

    @Test
    void enviarLembretes_falhaEmUmItemNaoDeveAbortarOLote() {
        Agendamento comFalha = agendamentoElegivel();
        Agendamento semFalha = agendamentoElegivel();
        when(agendamentoRepository.findElegiveisParaLembrete(any(), any()))
                .thenReturn(List.of(comFalha, semFalha));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioConectadoETemplateAprovado()));
        when(clienteRepository.findByIdAndUsuarioId(clienteId, usuarioId)).thenReturn(Optional.of(cliente()));
        org.mockito.Mockito.doThrow(new RuntimeException("Graph API fora do ar"))
                .doNothing()
                .when(graphApiClient)
                .enviarLembreteConfirmacao(anyString(), anyString(), anyString());

        job.enviarLembretes();

        verify(graphApiClient, times(2)).enviarLembreteConfirmacao(anyString(), anyString(), anyString());
        verify(agendamentoRepository, times(1)).save(any());
    }
}
