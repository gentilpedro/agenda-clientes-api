package com.agendaclientes.api.whatsapp.infrastructure.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agendaclientes.api.shared.openapi.OpenApiConfig;
import com.agendaclientes.api.usuario.domain.Usuario;
import com.agendaclientes.api.whatsapp.application.WhatsappConnectionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "WhatsApp", description = "Conexão do WhatsApp Business do consultório autenticado (Embedded Signup)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@ApiResponse(responseCode = "403", description = "Token ausente, expirado ou inválido")
@RestController
@RequestMapping("/api/whatsapp")
public class WhatsappConnectionController {

    private final WhatsappConnectionService whatsappConnectionService;
    private final WhatsappWebMapper mapper;

    public WhatsappConnectionController(WhatsappConnectionService whatsappConnectionService,
            WhatsappWebMapper mapper) {
        this.whatsappConnectionService = whatsappConnectionService;
        this.mapper = mapper;
    }

    @Operation(summary = "Status da conexão do WhatsApp do consultório autenticado")
    @GetMapping("/status")
    public WhatsappStatusResponse status(@AuthenticationPrincipal UUID usuarioId) {
        return mapper.toResponse(whatsappConnectionService.buscarStatus(usuarioId));
    }

    @Operation(summary = "Conclui o Embedded Signup",
            description = "Troca o código devolvido pelo popup da Meta por um token de acesso, "
                    + "assina o webhook e submete o template de confirmação de agendamento.")
    @PostMapping("/conectar")
    public WhatsappStatusResponse conectar(@AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody WhatsappConectarRequest request) {
        Usuario usuario = whatsappConnectionService.conectar(usuarioId, request.code(), request.wabaId(),
                request.phoneNumberId());
        return mapper.toResponse(usuario);
    }

    @Operation(summary = "Desconecta o WhatsApp do consultório autenticado")
    @ApiResponse(responseCode = "204", description = "Desconectado")
    @DeleteMapping
    public ResponseEntity<Void> desconectar(@AuthenticationPrincipal UUID usuarioId) {
        whatsappConnectionService.desconectar(usuarioId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
