package com.agendaclientes.api.cliente.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.agendaclientes.api.cliente.domain.Cliente;
import com.agendaclientes.api.cliente.domain.ClienteRepository;
import com.agendaclientes.api.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    private ClienteServiceImpl clienteService;

    private final UUID usuarioId = UUID.randomUUID();

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        clienteService = new ClienteServiceImpl(clienteRepository);
    }

    @Test
    void criar_deveSalvarNovoClienteDoUsuario() {
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cliente criado = clienteService.criar(usuarioId, "Maria", "maria@email.com", "11999999999", "obs");

        assertThat(criado.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(criado.getNome()).isEqualTo("Maria");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void atualizar_deveAtualizarDadosDeClienteExistente() {
        UUID id = UUID.randomUUID();
        Cliente existente = Cliente.existente(id, usuarioId, "Antigo", "antigo@email.com", "111", "obs",
                java.time.Instant.now());
        when(clienteRepository.findByIdAndUsuarioId(id, usuarioId)).thenReturn(Optional.of(existente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cliente atualizado = clienteService.atualizar(usuarioId, id, "Novo", "novo@email.com", "222", "obs2");

        assertThat(atualizado.getNome()).isEqualTo("Novo");
        assertThat(atualizado.getEmail()).isEqualTo("novo@email.com");
    }

    @Test
    void atualizar_deveLancarExcecaoQuandoClienteNaoPertenceAoUsuario() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.findByIdAndUsuarioId(id, usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.atualizar(usuarioId, id, "Novo", "novo@email.com", "222", "obs"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void buscarPorId_deveLancarExcecaoQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.findByIdAndUsuarioId(id, usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(usuarioId, id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void listarTodos_deveRetornarApenasClientesDoUsuario() {
        Cliente cliente = Cliente.existente(UUID.randomUUID(), usuarioId, "Maria", "maria@email.com", "111", null,
                java.time.Instant.now());
        when(clienteRepository.findAllByUsuarioId(usuarioId)).thenReturn(List.of(cliente));

        List<Cliente> resultado = clienteService.listarTodos(usuarioId);

        assertThat(resultado).containsExactly(cliente);
    }

    @Test
    void remover_deveLancarExcecaoQuandoClienteNaoExiste() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.existsByIdAndUsuarioId(id, usuarioId)).thenReturn(false);

        assertThatThrownBy(() -> clienteService.remover(usuarioId, id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(clienteRepository, never()).deleteById(any());
    }

    @Test
    void remover_deveDeletarQuandoClienteExiste() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.existsByIdAndUsuarioId(id, usuarioId)).thenReturn(true);

        clienteService.remover(usuarioId, id);

        verify(clienteRepository).deleteById(id);
    }
}
