package com.agendaclientes.api.cliente.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.agendaclientes.api.cliente.domain.Cliente;
import com.agendaclientes.api.cliente.domain.ClienteRepository;
import com.agendaclientes.api.shared.exception.ResourceNotFoundException;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Cliente criar(UUID usuarioId, String nome, String email, String telefone, String observacoes) {
        Cliente cliente = Cliente.novo(usuarioId, nome, email, telefone, observacoes);
        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente atualizar(UUID usuarioId, UUID id, String nome, String email, String telefone,
            String observacoes) {
        Cliente cliente = buscarPorId(usuarioId, id);
        cliente.atualizarDados(nome, email, telefone, observacoes);
        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente buscarPorId(UUID usuarioId, UUID id) {
        return clienteRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + id));
    }

    @Override
    public List<Cliente> listarTodos(UUID usuarioId) {
        return clienteRepository.findAllByUsuarioId(usuarioId);
    }

    @Override
    public void remover(UUID usuarioId, UUID id) {
        if (!clienteRepository.existsByIdAndUsuarioId(id, usuarioId)) {
            throw new ResourceNotFoundException("Cliente não encontrado: " + id);
        }
        clienteRepository.deleteById(id);
    }
}
