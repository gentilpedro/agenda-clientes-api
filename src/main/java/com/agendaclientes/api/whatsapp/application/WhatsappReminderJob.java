package com.agendaclientes.api.whatsapp.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.agendaclientes.api.agendamento.domain.Agendamento;
import com.agendaclientes.api.agendamento.domain.AgendamentoRepository;
import com.agendaclientes.api.cliente.domain.Cliente;
import com.agendaclientes.api.cliente.domain.ClienteRepository;
import com.agendaclientes.api.usuario.domain.Usuario;
import com.agendaclientes.api.usuario.domain.UsuarioRepository;
import com.agendaclientes.api.whatsapp.infrastructure.graph.GraphApiClient;

/**
 * Varre periodicamente os agendamentos que entraram na janela de antecedência
 * configurada e ainda não receberam lembrete, e dispara a mensagem de
 * confirmação via WhatsApp pra quem já está com a conexão aprovada.
 */
@Component
class WhatsappReminderJob {

    private static final Logger log = LoggerFactory.getLogger(WhatsappReminderJob.class);
    private static final String TEMPLATE_APROVADO = "APROVADO";

    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final GraphApiClient graphApiClient;
    private final long leadTimeMinutos;

    WhatsappReminderJob(AgendamentoRepository agendamentoRepository, UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository, GraphApiClient graphApiClient,
            @Value("${app.whatsapp.reminder-lead-time-minutes}") long leadTimeMinutos) {
        this.agendamentoRepository = agendamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.graphApiClient = graphApiClient;
        this.leadTimeMinutos = leadTimeMinutos;
    }

    @Scheduled(fixedDelayString = "${app.whatsapp.reminder-check-interval-ms}")
    void enviarLembretes() {
        Instant agora = Instant.now();
        Instant janelaFim = agora.plus(leadTimeMinutos, ChronoUnit.MINUTES);

        for (Agendamento agendamento : agendamentoRepository.findElegiveisParaLembrete(agora, janelaFim)) {
            try {
                processar(agendamento, agora);
            } catch (Exception e) {
                // Uma falha isolada (ex.: Graph API fora do ar pra um tenant) não pode
                // abortar o lote inteiro — os demais agendamentos elegíveis seguem.
                log.warn("Falha ao enviar lembrete do agendamento {}: {}", agendamento.getId(), e.getMessage());
            }
        }
    }

    private void processar(Agendamento agendamento, Instant agora) {
        Usuario usuario = usuarioRepository.findById(agendamento.getUsuarioId()).orElse(null);
        if (usuario == null || !usuario.whatsappConectado()
                || !TEMPLATE_APROVADO.equals(usuario.getWhatsappTemplateStatus())) {
            return;
        }
        Cliente cliente = clienteRepository.findByIdAndUsuarioId(agendamento.getClienteId(), usuario.getId())
                .orElse(null);
        if (cliente == null) {
            return;
        }

        graphApiClient.enviarLembreteConfirmacao(usuario.getWhatsappPhoneNumberId(), usuario.getWhatsappTokenAcesso(),
                cliente.getTelefone());

        agendamento.marcarLembreteEnviado(agora);
        agendamentoRepository.save(agendamento);
    }
}
