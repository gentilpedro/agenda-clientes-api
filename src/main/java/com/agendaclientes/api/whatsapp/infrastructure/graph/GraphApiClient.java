package com.agendaclientes.api.whatsapp.infrastructure.graph;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Fino wrapper sobre a Graph API da Meta (WhatsApp Cloud API). Fala com a
 * Meta em inglês e traduz o vocabulário dela (status de template em inglês,
 * por exemplo) pro vocabulário da aplicação — quem chama este cliente nunca
 * vê o formato bruto da resposta da Meta.
 */
@Component
public class GraphApiClient {

    private final RestClient restClient;
    private final String appId;
    private final String appSecret;
    private final String templateName;

    public GraphApiClient(RestClient whatsappRestClient,
            @Value("${app.whatsapp.app-id}") String appId,
            @Value("${app.whatsapp.app-secret}") String appSecret,
            @Value("${app.whatsapp.template-name}") String templateName) {
        this.restClient = whatsappRestClient;
        this.appId = appId;
        this.appSecret = appSecret;
        this.templateName = templateName;
    }

    /** Troca o {@code code} devolvido pelo Embedded Signup por um token de acesso de longa duração. */
    public String trocarCodePorToken(String code) {
        GraphTokenResponse resposta = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/oauth/access_token")
                        .queryParam("client_id", appId)
                        .queryParam("client_secret", appSecret)
                        .queryParam("code", code)
                        .build())
                .retrieve()
                .body(GraphTokenResponse.class);
        if (resposta == null || resposta.accessToken() == null) {
            throw new IllegalStateException("Resposta vazia da Meta ao trocar o código de autorização");
        }
        return resposta.accessToken();
    }

    /** Número (formatado para exibição) associado ao {@code phoneNumberId} conectado. */
    public String buscarNumeroExibicao(String phoneNumberId, String tokenAcesso) {
        GraphPhoneNumberResponse resposta = restClient.get()
                .uri("/{phoneNumberId}?fields=display_phone_number", phoneNumberId)
                .headers(headers -> headers.setBearerAuth(tokenAcesso))
                .retrieve()
                .body(GraphPhoneNumberResponse.class);
        return resposta != null ? resposta.displayPhoneNumber() : null;
    }

    /** Assina a WABA do consultório pra que os webhooks desta aplicação passem a receber os eventos dela. */
    public void assinarWebhook(String wabaId, String tokenAcesso) {
        restClient.post()
                .uri("/{wabaId}/subscribed_apps", wabaId)
                .headers(headers -> headers.setBearerAuth(tokenAcesso))
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Submete (ou re-submete) o template de confirmação de agendamento na WABA do
     * consultório, com dois botões de resposta rápida. Devolve o status traduzido
     * pro vocabulário da aplicação (PENDENTE/APROVADO/REJEITADO).
     */
    public String criarTemplateConfirmacao(String wabaId, String tokenAcesso) {
        Map<String, Object> corpo = Map.of(
                "name", templateName,
                "language", "pt_BR",
                "category", "UTILITY",
                "components", List.of(
                        Map.of("type", "BODY", "text",
                                "Você tem um agendamento em breve. Pode confirmar sua presença?"),
                        Map.of("type", "BUTTONS", "buttons", List.of(
                                Map.of("type", "QUICK_REPLY", "text", "Confirmar"),
                                Map.of("type", "QUICK_REPLY", "text", "Cancelar")))));

        GraphTemplateResponse resposta = restClient.post()
                .uri("/{wabaId}/message_templates", wabaId)
                .headers(headers -> headers.setBearerAuth(tokenAcesso))
                .body(corpo)
                .retrieve()
                .body(GraphTemplateResponse.class);
        return traduzirStatusTemplate(resposta != null ? resposta.status() : null);
    }

    /** Envia o lembrete de confirmação (template com botões) pro telefone do cliente. */
    public void enviarLembreteConfirmacao(String phoneNumberId, String tokenAcesso, String numeroDestino) {
        Map<String, Object> corpo = Map.of(
                "messaging_product", "whatsapp",
                "to", numeroDestino,
                "type", "template",
                "template", Map.of(
                        "name", templateName,
                        "language", Map.of("code", "pt_BR")));

        restClient.post()
                .uri("/{phoneNumberId}/messages", phoneNumberId)
                .headers((HttpHeaders headers) -> headers.setBearerAuth(tokenAcesso))
                .body(corpo)
                .retrieve()
                .toBodilessEntity();
    }

    private static String traduzirStatusTemplate(String statusGraph) {
        if (statusGraph == null) {
            return "PENDENTE";
        }
        return switch (statusGraph) {
            case "APPROVED" -> "APROVADO";
            case "REJECTED" -> "REJEITADO";
            default -> "PENDENTE";
        };
    }

    private record GraphTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    private record GraphPhoneNumberResponse(@JsonProperty("display_phone_number") String displayPhoneNumber) {
    }

    private record GraphTemplateResponse(String id, String status) {
    }
}
