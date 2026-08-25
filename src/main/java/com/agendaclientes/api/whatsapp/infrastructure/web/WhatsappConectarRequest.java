package com.agendaclientes.api.whatsapp.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record WhatsappConectarRequest(
        @NotBlank(message = "Código é obrigatório")
        @Schema(description = "Código de autorização devolvido pelo Embedded Signup") String code,

        @NotBlank(message = "WABA é obrigatória")
        @Schema(description = "Id da WhatsApp Business Account conectada") String wabaId,

        @NotBlank(message = "Phone number id é obrigatório")
        @Schema(description = "Id do número de telefone conectado nessa WABA") String phoneNumberId) {
}
