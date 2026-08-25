package com.agendaclientes.api.whatsapp.application;

import java.util.Comparator;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.agendaclientes.api.agendamento.domain.Agendamento;
import com.agendaclientes.api.agendamento.domain.AgendamentoRepository;
import com.agendaclientes.api.agendamento.domain.AgendamentoStatus;
import com.agendaclientes.api.cliente.domain.Cliente;
import com.agendaclientes.api.cliente.domain.ClienteRepository;
import com.agendaclientes.api.usuario.domain.Usuario;
import com.agendaclientes.api.usuario.domain.UsuarioRepository;

@Service
public class WhatsappWebhookServiceImpl implements WhatsappWebhookService {

    private static final Logger log = LoggerFactory.getLogger(WhatsappWebhookServiceImpl.class);

    private static final String BOTAO_CONFIRMAR = "CONFIRMAR";
    private static final String BOTAO_CANCELAR = "CANCELAR";

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final TelefoneMatcher telefoneMatcher;

    public WhatsappWebhookServiceImpl(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository,
            AgendamentoRepository agendamentoRepository, TelefoneMatcher telefoneMatcher) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.telefoneMatcher = telefoneMatcher;
    }

    @Override
    public void processarRespostaBotao(String phoneNumberId, String numeroRemetente, String idBotao) {
        if (!BOTAO_CONFIRMAR.equals(idBotao) && !BOTAO_CANCELAR.equals(idBotao)) {
            log.warn("Botão desconhecido recebido no webhook do WhatsApp: {}", idBotao);
            return;
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByWhatsappPhoneNumberId(phoneNumberId);
        if (usuarioOpt.isEmpty()) {
            log.info("Webhook recebido para phone_number_id não conectado a nenhum consultório: {}", phoneNumberId);
            return;
        }
        Usuario usuario = usuarioOpt.get();

        Optional<Cliente> clienteOpt = clienteRepository.findAllByUsuarioId(usuario.getId()).stream()
                .filter(cliente -> telefoneMatcher.correspondem(cliente.getTelefone(), numeroRemetente))
                .findFirst();
        if (clienteOpt.isEmpty()) {
            log.info("Nenhum cliente do usuário {} corresponde ao telefone {}", usuario.getId(), numeroRemetente);
            return;
        }
        Cliente cliente = clienteOpt.get();

        Optional<Agendamento> agendamentoOpt = agendamentoRepository
                .findByUsuarioIdAndClienteIdAndStatus(usuario.getId(), cliente.getId(), AgendamentoStatus.AGENDADO)
                .stream()
                .filter(agendamento -> agendamento.getLembreteEnviadoEm() != null && !agendamento.isConfirmado())
                .min(Comparator.comparing(Agendamento::getDataHora));
        if (agendamentoOpt.isEmpty()) {
            log.info("Nenhum agendamento aguardando confirmação para o cliente {}", cliente.getId());
            return;
        }
        Agendamento agendamento = agendamentoOpt.get();

        if (BOTAO_CONFIRMAR.equals(idBotao)) {
            agendamento.confirmar();
        } else {
            agendamento.cancelar();
        }
        agendamentoRepository.save(agendamento);
    }
}
