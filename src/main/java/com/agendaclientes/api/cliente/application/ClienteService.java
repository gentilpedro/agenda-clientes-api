package com.agendaclientes.api.cliente.application;

import java.util.List;
import java.util.UUID;

import com.agendaclientes.api.cliente.domain.Cliente;

public interface ClienteService {

    Cliente criar(UUID usuarioId, String nome, String email, String telefone, String observacoes);

    Cliente atualizar(UUID usuarioId, UUID id, String nome, String email, String telefone, String observacoes);

    Cliente buscarPorId(UUID usuarioId, UUID id);

    List<Cliente> listarTodos(UUID usuarioId);

    void remover(UUID usuarioId, UUID id);
}
