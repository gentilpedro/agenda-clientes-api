package com.agendaclientes.api.whatsapp.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.agendaclientes.api.shared.exception.ResourceNotFoundException;
import com.agendaclientes.api.usuario.domain.Usuario;
import com.agendaclientes.api.usuario.domain.UsuarioRepository;
import com.agendaclientes.api.whatsapp.infrastructure.graph.GraphApiClient;

@Service
public class WhatsappConnectionServiceImpl implements WhatsappConnectionService {

    private final UsuarioRepository usuarioRepository;
    private final GraphApiClient graphApiClient;

    public WhatsappConnectionServiceImpl(UsuarioRepository usuarioRepository, GraphApiClient graphApiClient) {
        this.usuarioRepository = usuarioRepository;
        this.graphApiClient = graphApiClient;
    }

    @Override
    public Usuario buscarStatus(UUID usuarioId) {
        return buscarUsuario(usuarioId);
    }

    @Override
    public Usuario conectar(UUID usuarioId, String code, String wabaId, String phoneNumberId) {
        Usuario usuario = buscarUsuario(usuarioId);

        String tokenAcesso = graphApiClient.trocarCodePorToken(code);
        String numeroExibicao = graphApiClient.buscarNumeroExibicao(phoneNumberId, tokenAcesso);
        usuario.conectarWhatsapp(wabaId, phoneNumberId, numeroExibicao, tokenAcesso, Instant.now());
        usuario = usuarioRepository.save(usuario);

        graphApiClient.assinarWebhook(wabaId, tokenAcesso);
        String statusTemplate = graphApiClient.criarTemplateConfirmacao(wabaId, tokenAcesso);
        usuario.atualizarStatusTemplate(statusTemplate);
        return usuarioRepository.save(usuario);
    }

    @Override
    public void desconectar(UUID usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        usuario.desconectarWhatsapp();
        usuarioRepository.save(usuario);
    }

    private Usuario buscarUsuario(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + usuarioId));
    }
}
