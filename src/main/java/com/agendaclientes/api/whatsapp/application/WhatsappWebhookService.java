package com.agendaclientes.api.whatsapp.application;

public interface WhatsappWebhookService {

    /**
     * Aplica a resposta de um botão de confirmação ao agendamento correspondente.
     * Nunca lança para "não encontrado" (tenant/cliente/agendamento) — apenas loga
     * e não faz nada, já que isso é esperado (recibos duplicados, respostas
     * tardias, etc.) e a Meta não deve receber erro por isso.
     */
    void processarRespostaBotao(String phoneNumberId, String numeroRemetente, String idBotao);
}
