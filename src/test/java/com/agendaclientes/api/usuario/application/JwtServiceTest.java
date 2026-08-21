package com.agendaclientes.api.usuario.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private static final String SECRET = "chave-de-teste-com-tamanho-minimo-de-32-bytes-ok";

    private final JwtService jwtService = new JwtService(SECRET, 60);

    @Test
    void gerarTokenEValidar_deveRoundTriparUsuarioId() {
        UUID usuarioId = UUID.randomUUID();

        String token = jwtService.gerarToken(usuarioId, "maria@email.com");
        UUID extraido = jwtService.validarEExtrairUsuarioId(token);

        assertThat(extraido).isEqualTo(usuarioId);
    }

    @Test
    void validarEExtrairUsuarioId_deveLancarExcecaoQuandoAssinaturaNaoConfere() {
        UUID usuarioId = UUID.randomUUID();
        String token = jwtService.gerarToken(usuarioId, "maria@email.com");

        JwtService outroServicoComOutraChave = new JwtService(
                "outra-chave-de-teste-tambem-com-32-bytes-min", 60);

        assertThatThrownBy(() -> outroServicoComOutraChave.validarEExtrairUsuarioId(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void validarEExtrairUsuarioId_deveLancarExcecaoQuandoTokenMalformado() {
        assertThatThrownBy(() -> jwtService.validarEExtrairUsuarioId("token-invalido"))
                .isInstanceOf(JwtException.class);
    }
}
