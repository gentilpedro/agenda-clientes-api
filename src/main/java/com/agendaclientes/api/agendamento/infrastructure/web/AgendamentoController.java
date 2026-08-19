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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final AgendamentoWebMapper mapper;

    public AgendamentoController(AgendamentoService agendamentoService, AgendamentoWebMapper mapper) {
        this.agendamentoService = agendamentoService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<AgendamentoResponse> listar(
            @AuthenticationPrincipal UUID usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<Agendamento> agendamentos = data != null
                ? agendamentoService.listarPorData(usuarioId, data)
                : agendamentoService.listarTodos(usuarioId);
        return agendamentos.stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public AgendamentoResponse buscarPorId(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id) {
        return mapper.toResponse(agendamentoService.buscarPorId(usuarioId, id));
    }

    @PostMapping
    public ResponseEntity<AgendamentoResponse> criar(@AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody AgendamentoRequest request) {
        Agendamento criado = agendamentoService.criar(usuarioId, request.clienteId(), request.dataHora(),
                request.duracaoMinutos(), request.observacoes());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(criado));
    }

    @PutMapping("/{id}")
    public AgendamentoResponse atualizar(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id,
            @Valid @RequestBody AgendamentoRequest request) {
        Agendamento atualizado = agendamentoService.atualizar(usuarioId, id, request.dataHora(),
                request.duracaoMinutos(), request.observacoes());
        return mapper.toResponse(atualizado);
    }

    @PatchMapping("/{id}/cancelar")
    public AgendamentoResponse cancelar(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id) {
        return mapper.toResponse(agendamentoService.cancelar(usuarioId, id));
    }

    @PatchMapping("/{id}/concluir")
    public AgendamentoResponse concluir(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id) {
        return mapper.toResponse(agendamentoService.concluir(usuarioId, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID id) {
        agendamentoService.remover(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
