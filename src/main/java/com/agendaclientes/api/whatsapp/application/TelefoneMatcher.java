package com.agendaclientes.api.whatsapp.application;

import org.springframework.stereotype.Component;

/**
 * Compara números de telefone ignorando formatação e prefixo de DDI ausente —
 * cadastros antigos de cliente podem não ter o "+55" que o WhatsApp sempre
 * manda no {@code from} da mensagem.
 */
@Component
public class TelefoneMatcher {

    public boolean correspondem(String telefoneCliente, String telefoneWhatsapp) {
        if (telefoneCliente == null || telefoneWhatsapp == null) {
            return false;
        }
        String digitosCliente = apenasDigitos(telefoneCliente);
        String digitosWhatsapp = apenasDigitos(telefoneWhatsapp);
        if (digitosCliente.isEmpty() || digitosWhatsapp.isEmpty()) {
            return false;
        }
        return digitosCliente.endsWith(digitosWhatsapp) || digitosWhatsapp.endsWith(digitosCliente);
    }

    private static String apenasDigitos(String telefone) {
        return telefone.replaceAll("\\D", "");
    }
}
