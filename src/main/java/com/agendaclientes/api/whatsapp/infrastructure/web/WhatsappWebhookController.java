package com.agendaclientes.api.whatsapp.infrastructure.web;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.agendaclientes.api.whatsapp.application.WhatsappWebhookService;
import com.agendaclientes.api.whatsapp.infrastructure.web.WhatsappWebhookPayload.WhatsappChange;
import com.agendaclientes.api.whatsapp.infrastructure.web.WhatsappWebhookPayload.WhatsappEntry;
import com.agendaclientes.api.whatsapp.infrastructure.web.WhatsappWebhookPayload.WhatsappMessage;
import com.agendaclientes.api.whatsapp.infrastructure.web.WhatsappWebhookPayload.WhatsappValue;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Recebe os eventos que a Meta envia pra WABA de qualquer consultório
 * conectado. É pública (ver SecurityConfig) — a Meta não manda JWT — e
 * autentica a requisição sozinha via HMAC (POST) ou o token de verificação
 * (GET, handshake único ao configurar a URL do webhook).
 * <p>
 * O handler de POST NUNCA deixa uma exceção de negócio propagar: a Meta
 * reenvia agressivamente (e pode desativar a assinatura do webhook) em
 * qualquer resposta diferente de 200.
 */
@Tag(name = "WhatsApp", description = "Webhook de eventos do WhatsApp Cloud API, chamado pela Meta")
@RestController
@RequestMapping("/api/whatsapp/webhook")
public class WhatsappWebhookController {

    private static final Logger log = LoggerFactory.getLogger(WhatsappWebhookController.class);
    private static final String PREFIXO_ASSINATURA = "sha256=";

    private final WhatsappWebhookService whatsappWebhookService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String appSecret;
    private final String verifyToken;

    public WhatsappWebhookController(WhatsappWebhookService whatsappWebhookService,
            @Value("${app.whatsapp.app-secret}") String appSecret,
            @Value("${app.whatsapp.webhook-verify-token}") String verifyToken) {
        this.whatsappWebhookService = whatsappWebhookService;
        this.appSecret = appSecret;
        this.verifyToken = verifyToken;
    }

    @Operation(summary = "Handshake de verificação, chamado uma única vez pela Meta ao configurar a URL do webhook")
    @GetMapping
    public ResponseEntity<String> verificar(
            @RequestParam("hub.mode") String modo,
            @RequestParam("hub.verify_token") String tokenRecebido,
            @RequestParam("hub.challenge") String challenge) {
        if (!"subscribe".equals(modo) || !verifyToken.equals(tokenRecebido)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(challenge);
    }

    @Operation(summary = "Recebe mensagens/respostas de botão e recibos de status do WhatsApp",
            description = "Sempre responde 200 (exceto assinatura inválida), mesmo quando o evento não pôde "
                    + "ser aplicado a nenhum agendamento.")
    @PostMapping
    public ResponseEntity<Void> receber(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String assinatura,
            @RequestBody String corpoBruto) {
        if (!assinaturaValida(assinatura, corpoBruto)) {
            log.warn("Webhook do WhatsApp recebido com assinatura inválida ou ausente");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            processarPayload(corpoBruto);
        } catch (Exception e) {
            log.warn("Falha ao processar evento do webhook do WhatsApp: {}", e.getMessage(), e);
        }
        return ResponseEntity.ok().build();
    }

    private void processarPayload(String corpoBruto) throws Exception {
        WhatsappWebhookPayload payload = objectMapper.readValue(corpoBruto, WhatsappWebhookPayload.class);
        for (WhatsappEntry entry : orVazia(payload.entry())) {
            for (WhatsappChange change : orVazia(entry.changes())) {
                processarValue(change.value());
            }
        }
    }

    private void processarValue(WhatsappValue value) {
        if (value == null || value.metadata() == null) {
            return;
        }
        String phoneNumberId = value.metadata().phoneNumberId();
        for (WhatsappMessage mensagem : orVazia(value.messages())) {
            if (mensagem.interactive() == null || mensagem.interactive().buttonReply() == null) {
                // Recibo de status (statuses[]) ou resposta em texto livre — não há botão pra processar.
                continue;
            }
            whatsappWebhookService.processarRespostaBotao(phoneNumberId, mensagem.from(),
                    mensagem.interactive().buttonReply().id());
        }
    }

    private static <T> List<T> orVazia(List<T> lista) {
        return lista != null ? lista : List.of();
    }

    private boolean assinaturaValida(String assinaturaRecebida, String corpoBruto) {
        if (assinaturaRecebida == null || !assinaturaRecebida.startsWith(PREFIXO_ASSINATURA)) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] assinaturaCalculada = mac.doFinal(corpoBruto.getBytes(StandardCharsets.UTF_8));
            String assinaturaEsperada = PREFIXO_ASSINATURA + HexFormat.of().formatHex(assinaturaCalculada);
            return MessageDigest.isEqual(assinaturaEsperada.getBytes(StandardCharsets.UTF_8),
                    assinaturaRecebida.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            log.error("Erro ao validar a assinatura do webhook do WhatsApp", e);
            return false;
        }
    }
}
