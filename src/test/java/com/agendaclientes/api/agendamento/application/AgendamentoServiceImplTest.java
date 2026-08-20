package com.agendaclientes.api.agendamento.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.agendaclientes.api.agendamento.domain.Agendamento;
import com.agendaclientes.api.agendamento.domain.AgendamentoRepository;
import com.agendaclientes.api.agendamento.domain.AgendamentoStatus;
import com.agendaclientes.api.cliente.domain.ClienteRepository;
import com.agendaclientes.api.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceImplTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    private AgendamentoServiceImpl agendamentoService;

    private final UUID usuarioId = UUID.randomUUID();
    private final UUID clienteId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        agendamentoService = new AgendamentoServiceImpl(agendamentoRepository, clienteRepository);
    }

    @Test
    void criar_deveLancarExcecaoQuandoClienteNaoPertenceAoUsuario() {
        when(clienteRepository.existsByIdAndUsuarioId(clienteId, usuarioId)).thenReturn(false);

        assertThatThrownBy(() -> agendamentoService.criar(usuarioId, clienteId, Instant.now(), 30, null))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void criar_deveSalvarQuandoClientePertenceAoUsuario() {
        when(clienteRepository.existsByIdAndUsuarioId(clienteId, usuarioId)).thenReturn(true);
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Agendamento criado = agendamentoService.criar(usuarioId, clienteId, Instant.now(), 30, "obs");

        assertThat(criado.getStatus()).isEqualTo(AgendamentoStatus.AGENDADO);
        assertThat(criado.getClienteId()).isEqualTo(clienteId);
    }

    @Test
    void buscarPorId_deveLancarExcecaoQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(agendamentoRepository.findByIdAndUsuarioId(id, usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendamentoService.buscarPorId(usuarioId, id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelar_deveLancarExcecaoQuandoAgendamentoJaConcluido() {
        UUID id = UUID.randomUUID();
        Agendamento concluido = Agendamento.existente(id, usuarioId, clienteId, Instant.now(), 30,
                AgendamentoStatus.CONCLUIDO, null);
        when(agendamentoRepository.findByIdAndUsuarioId(id, usuarioId)).thenReturn(Optional.of(concluido));

        assertThatThrownBy(() -> agendamentoService.cancelar(usuarioId, id))
                .isInstanceOf(IllegalStateException.class);
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void concluir_deveLancarExcecaoQuandoAgendamentoCancelado() {
        UUID id = UUID.randomUUID();
        Agendamento cancelado = Agendamento.existente(id, usuarioId, clienteId, Instant.now(), 30,
                AgendamentoStatus.CANCELADO, null);
        when(agendamentoRepository.findByIdAndUsuarioId(id, usuarioId)).thenReturn(Optional.of(cancelado));

        assertThatThrownBy(() -> agendamentoService.concluir(usuarioId, id))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void concluir_deveAtualizarStatusQuandoAgendamentoEstaAgendado() {
        UUID id = UUID.randomUUID();
        Agendamento agendado = Agendamento.existente(id, usuarioId, clienteId, Instant.now(), 30,
                AgendamentoStatus.AGENDADO, null);
        when(agendamentoRepository.findByIdAndUsuarioId(id, usuarioId)).thenReturn(Optional.of(agendado));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Agendamento concluido = agendamentoService.concluir(usuarioId, id);

        assertThat(concluido.getStatus()).isEqualTo(AgendamentoStatus.CONCLUIDO);
    }

    @Test
    void listarPorData_deveConsultarPeriodoDeUmDiaInteiro() {
        LocalDate data = LocalDate.of(2026, 3, 10);
        ZoneId zone = ZoneId.systemDefault();
        Instant inicioEsperado = data.atStartOfDay(zone).toInstant();
        Instant fimEsperado = data.plusDays(1).atStartOfDay(zone).toInstant();

        agendamentoService.listarPorData(usuarioId, data);

        verify(agendamentoRepository).findByUsuarioIdAndPeriodo(eq(usuarioId), eq(inicioEsperado), eq(fimEsperado));
    }

    @Test
    void remover_deveLancarExcecaoQuandoNaoExiste() {
        UUID id = UUID.randomUUID();
        when(agendamentoRepository.existsByIdAndUsuarioId(id, usuarioId)).thenReturn(false);

        assertThatThrownBy(() -> agendamentoService.remover(usuarioId, id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(agendamentoRepository, never()).deleteById(any());
    }
}
