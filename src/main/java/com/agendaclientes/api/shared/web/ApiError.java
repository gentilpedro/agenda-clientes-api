package com.agendaclientes.api.shared.web;

import java.time.Instant;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Formato padrão de erro da API")
public record ApiError(
        Instant timestamp,
        int status,
        String message,
        @Schema(description = "Erros por campo (nome do campo -> mensagem). Preenchido apenas em erros de validação; "
                + "vazio nos demais casos.") Map<String, String> fieldErrors) {

    public static ApiError of(int status, String message) {
        return new ApiError(Instant.now(), status, message, Map.of());
    }

    public static ApiError of(int status, String message, Map<String, String> fieldErrors) {
        return new ApiError(Instant.now(), status, message, fieldErrors);
    }
}
