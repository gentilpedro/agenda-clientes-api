package com.agendaclientes.api.whatsapp.infrastructure.web;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Modela só o que a aplicação precisa do payload de webhook da Meta — o
 * mesmo POST também carrega recibos de status ({@code statuses}), contatos
 * etc., ignorados aqui de propósito.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WhatsappWebhookPayload(String object, List<WhatsappEntry> entry) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WhatsappEntry(String id, List<WhatsappChange> changes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WhatsappChange(WhatsappValue value, String field) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WhatsappValue(WhatsappMetadata metadata, List<WhatsappMessage> messages) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WhatsappMetadata(@JsonProperty("phone_number_id") String phoneNumberId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WhatsappMessage(String from, String type, WhatsappInteractive interactive) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WhatsappInteractive(String type, @JsonProperty("button_reply") WhatsappButtonReply buttonReply) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WhatsappButtonReply(String id, String title) {
    }
}
