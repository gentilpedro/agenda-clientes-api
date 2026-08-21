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
import com.agendaclientes.api.shared.openapi.OpenApiConfig;
import com.agendaclientes.api.shared.web.ApiError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Clientes", description = "Cadastro dos clientes do usuário autenticado")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@ApiResponse(responseCode = "403", description = "Token ausente, expirado ou inválido", content = @Content)
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteWebMapper mapper;

    public ClienteController(ClienteService clienteService, ClienteWebMapper mapper) {
        this.clienteService = clienteService;
        this.mapper = mapper;
    }

    @Operation(summary = "Lista os clientes do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Clientes encontrados")
    @GetMapping
    public List<ClienteResponse> listar(@AuthenticationPrincipal UUID usuarioId) {
        return clienteService.listarTodos(usuarioId).stream().map(mapper::toResponse).toList();
    }

    @Operation(summary = "Busca um cliente pelo id")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{id}")
    public ClienteResponse buscarPorId(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id) {
        return mapper.toResponse(clienteService.buscarPorId(usuarioId, id));
    }

    @Operation(summary = "Cadastra um cliente")
    @ApiResponse(responseCode = "201", description = "Cliente criado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody ClienteRequest request) {
        Cliente criado = clienteService.criar(usuarioId, request.nome(), request.email(), request.telefone(),
                request.observacoes());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(criado));
    }

    @Operation(summary = "Atualiza os dados de um cliente")
    @ApiResponse(responseCode = "200", description = "Cliente atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/{id}")
    public ClienteResponse atualizar(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id,
            @Valid @RequestBody ClienteRequest request) {
        Cliente atualizado = clienteService.atualizar(usuarioId, id, request.nome(), request.email(),
                request.telefone(), request.observacoes());
        return mapper.toResponse(atualizado);
    }

    @Operation(summary = "Remove um cliente")
    @ApiResponse(responseCode = "204", description = "Cliente removido", content = @Content)
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id) {
        clienteService.remover(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
