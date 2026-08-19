package com.agendaclientes.api.cliente.infrastructure.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agendaclientes.api.cliente.application.ClienteService;
import com.agendaclientes.api.cliente.domain.Cliente;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteWebMapper mapper;

    public ClienteController(ClienteService clienteService, ClienteWebMapper mapper) {
        this.clienteService = clienteService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<ClienteResponse> listar(@AuthenticationPrincipal UUID usuarioId) {
        return clienteService.listarTodos(usuarioId).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ClienteResponse buscarPorId(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id) {
        return mapper.toResponse(clienteService.buscarPorId(usuarioId, id));
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody ClienteRequest request) {
        Cliente criado = clienteService.criar(usuarioId, request.nome(), request.email(), request.telefone(),
                request.observacoes());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(criado));
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id,
            @Valid @RequestBody ClienteRequest request) {
        Cliente atualizado = clienteService.atualizar(usuarioId, id, request.nome(), request.email(),
                request.telefone(), request.observacoes());
        return mapper.toResponse(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id) {
        clienteService.remover(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
