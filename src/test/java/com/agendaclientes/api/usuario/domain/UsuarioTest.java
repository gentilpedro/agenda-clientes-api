package com.agendaclientes.api.usuario.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class UsuarioTest {

    private Usuario novoUsuario() {
        return Usuario.existente(UUID.randomUUID(), "Maria", "maria@email.com", "hash", Instant.now(), null, null);
    }

    @Test
    void whatsappConectado_deveSerFalsoParaUsuarioNovo() {
        assertThat(novoUsuario().whatsappConectado()).isFalse();
    }

    @Test
    void conectarWhatsapp_deveGuardarDadosDaConexaoEIniciarTemplatePendente() {
        Usuario usuario = novoUsuario();
        Instant agora = Instant.now();

        usuario.conectarWhatsapp("waba-1", "phone-1", "+55 11 99999-0000", "token-plano", agora);

        assertThat(usuario.whatsappConectado()).isTrue();
        assertThat(usuario.getWhatsappWabaId()).isEqualTo("waba-1");
        assertThat(usuario.getWhatsappPhoneNumberId()).isEqualTo("phone-1");
        assertThat(usuario.getWhatsappNumeroExibicao()).isEqualTo("+55 11 99999-0000");
        assertThat(usuario.getWhatsappTokenAcesso()).isEqualTo("token-plano");
        assertThat(usuario.getWhatsappConectadoEm()).isEqualTo(agora);
        assertThat(usuario.getWhatsappTemplateStatus()).isEqualTo("PENDENTE");
    }

    @Test
    void desconectarWhatsapp_deveLimparTodosOsCamposDeConexao() {
        Usuario usuario = novoUsuario();
        usuario.conectarWhatsapp("waba-1", "phone-1", "+55 11 99999-0000", "token-plano", Instant.now());

        usuario.desconectarWhatsapp();

        assertThat(usuario.whatsappConectado()).isFalse();
        assertThat(usuario.getWhatsappWabaId()).isNull();
        assertThat(usuario.getWhatsappPhoneNumberId()).isNull();
        assertThat(usuario.getWhatsappNumeroExibicao()).isNull();
        assertThat(usuario.getWhatsappTokenAcesso()).isNull();
        assertThat(usuario.getWhatsappConectadoEm()).isNull();
        assertThat(usuario.getWhatsappTemplateStatus()).isNull();
    }

    @Test
    void atualizarStatusTemplate_deveAtualizarSomenteOStatus() {
        Usuario usuario = novoUsuario();
        usuario.conectarWhatsapp("waba-1", "phone-1", "+55 11 99999-0000", "token-plano", Instant.now());

        usuario.atualizarStatusTemplate("APROVADO");

        assertThat(usuario.getWhatsappTemplateStatus()).isEqualTo("APROVADO");
        assertThat(usuario.whatsappConectado()).isTrue();
    }
}
