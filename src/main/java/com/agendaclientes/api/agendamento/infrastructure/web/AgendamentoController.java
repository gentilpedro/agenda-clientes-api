package com.agendaclientes.api.agendamento.infrastructure.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.agendaclientes.api.agendamento.application.AgendamentoService;
import com.agendaclientes.api.agendamento.domain.Agendamento;
import com.agendaclientes.api.shared.openapi.OpenApiConfig;
import com.agendaclientes.api.shared.web.ApiError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Agendamentos", description = "Agenda de compromissos com os clientes do usuário autenticado")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@ApiResponse(responseCode = "403", description = "Token ausente, expirado ou inválido", content = @Content)
@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final AgendamentoWebMapper mapper;

    public AgendamentoController(AgendamentoService agendamentoService, AgendamentoWebMapper mapper) {
        this.agendamentoService = agendamentoService;
        this.mapper = mapper;
    }

    @Operation(summary = "Lista os agendamentos do usuário autenticado",
            description = "Sem o parâmetro `data`, devolve todos os agendamentos.")
    @ApiResponse(responseCode = "200", description = "Agendamentos encontrados")
    @GetMapping
    public List<AgendamentoResponse> listar(
            @AuthenticationPrincipal UUID usuarioId,
            @Parameter(description = "Filtra os agendamentos de um único dia (formato ISO: aaaa-MM-dd)",
                    example = "2026-08-20") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<Agendamento> agendamentos = data != null
                ? agendamentoService.listarPorData(usuarioId, data)
                : agendamentoService.listarTodos(usuarioId);
        return agendamentos.stream().map(mapper::toResponse).toList();
    }

    @Operation(summary = "Busca um agendamento pelo id")
    @ApiResponse(responseCode = "200", description = "Agendamento encontrado")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{id}")
    public AgendamentoResponse buscarPorId(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id) {
        return mapper.toResponse(agendamentoService.buscarPorId(usuarioId, id));
    }

    @Operation(summary = "Cria um agendamento para um cliente")
    @ApiResponse(responseCode = "201", description = "Agendamento criado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    public ResponseEntity<AgendamentoResponse> criar(@AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody AgendamentoRequest request) {
        Agendamento criado = agendamentoService.criar(usuarioId, request.clienteId(), request.dataHora(),
                request.duracaoMinutos(), request.observacoes());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(criado));
    }

    @Operation(summary = "Reagenda um agendamento",
            description = "Altera data/hora, duração e observações. O cliente do agendamento não muda.")
    @ApiResponse(responseCode = "200", description = "Agendamento atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/{id}")
    public AgendamentoResponse atualizar(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id,
            @Valid @RequestBody AgendamentoRequest request) {
        Agendamento atualizado = agendamentoService.atualizar(usuarioId, id, request.dataHora(),
                request.duracaoMinutos(), request.observacoes());
        return mapper.toResponse(atualizado);
    }

    @Operation(summary = "Cancela um agendamento", description = "Muda o status para CANCELADO.")
    @ApiResponse(responseCode = "200", description = "Agendamento cancelado")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PatchMapping("/{id}/cancelar")
    public AgendamentoResponse cancelar(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id) {
        return mapper.toResponse(agendamentoService.cancelar(usuarioId, id));
    }

    @Operation(summary = "Conclui um agendamento", description = "Muda o status para CONCLUIDO.")
    @ApiResponse(responseCode = "200", description = "Agendamento concluído")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PatchMapping("/{id}/concluir")
    public AgendamentoResponse concluir(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id) {
        return mapper.toResponse(agendamentoService.concluir(usuarioId, id));
    }

    @Operation(summary = "Remove um agendamento")
    @ApiResponse(responseCode = "204", description = "Agendamento removido", content = @Content)
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id) {
        agendamentoService.remover(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
