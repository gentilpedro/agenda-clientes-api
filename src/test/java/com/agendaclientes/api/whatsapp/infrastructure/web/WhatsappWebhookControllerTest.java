package com.agendaclientes.api.whatsapp.infrastructure.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.agendaclientes.api.usuario.application.JwtService;
import com.agendaclientes.api.whatsapp.application.WhatsappWebhookService;

/**
 * Igual à observação em AgendamentoControllerTest: a fatia @WebMvcTest não
 * carrega a SecurityConfig real, então os filtros são desligados; JwtService
 * segue importado só porque JwtAuthFilter (sempre presente nessas fatias)
 * depende dele pra ser instanciado.
 */
@WebMvcTest(WhatsappWebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(JwtService.class)
@TestPropertySource(properties = {
        "app.whatsapp.app-secret=segredo-de-teste",
        "app.whatsapp.webhook-verify-token=token-de-verificacao-teste"
})
class WhatsappWebhookControllerTest {

    private static final String SEGREDO = "segredo-de-teste";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WhatsappWebhookService whatsappWebhookService;

    @Test
    void verificar_deveEcoarChallengeQuandoTokenCorreto() throws Exception {
        mockMvc.perform(get("/api/whatsapp/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "token-de-verificacao-teste")
                        .param("hub.challenge", "abc123"))
                .andExpect(status().isOk())
                .andExpect(content().string("abc123"));
    }

    @Test
    void verificar_deveRetornar403QuandoTokenErrado() throws Exception {
        mockMvc.perform(get("/api/whatsapp/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "errado")
                        .param("hub.challenge", "abc123"))
                .andExpect(status().isForbidden());
    }

    @Test
    void receber_deveRetornar403QuandoAssinaturaInvalida() throws Exception {
        String corpo = "{\"object\":\"whatsapp_business_account\",\"entry\":[]}";

        mockMvc.perform(post("/api/whatsapp/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", "sha256=assinaturainvalida")
                        .content(corpo))
                .andExpect(status().isForbidden());
        verifyNoInteractions(whatsappWebhookService);
    }

    @Test
    void receber_deveRetornar403QuandoAssinaturaAusente() throws Exception {
        mockMvc.perform(post("/api/whatsapp/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void receber_deveProcessarRespostaDeBotaoQuandoAssinaturaValida() throws Exception {
        String corpo = payloadComBotao("CONFIRMAR");

        mockMvc.perform(post("/api/whatsapp/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", assinar(corpo))
                        .content(corpo))
                .andExpect(status().isOk());

        verify(whatsappWebhookService).processarRespostaBotao("phone-123", "5511999999999", "CONFIRMAR");
    }

    @Test
    void receber_deveResponder200MesmoQuandoServicoLancaExcecaoDeDominio() throws Exception {
        String corpo = payloadComBotao("CANCELAR");
        doThrow(new IllegalStateException("Não é possível cancelar um agendamento já concluído"))
                .when(whatsappWebhookService)
                .processarRespostaBotao(eq("phone-123"), eq("5511999999999"), eq("CANCELAR"));

        mockMvc.perform(post("/api/whatsapp/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", assinar(corpo))
                        .content(corpo))
                .andExpect(status().isOk());
    }

    @Test
    void receber_deveIgnorarReciboDeStatusSemMensagem() throws Exception {
        String corpo = """
                {
                  "object": "whatsapp_business_account",
                  "entry": [{
                    "id": "waba-1",
                    "changes": [{
                      "field": "messages",
                      "value": {
                        "metadata": { "phone_number_id": "phone-123" },
                        "statuses": [{ "id": "wamid.1", "status": "delivered" }]
                      }
                    }]
                  }]
                }
                """;

        mockMvc.perform(post("/api/whatsapp/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", assinar(corpo))
                        .content(corpo))
                .andExpect(status().isOk());
        verifyNoInteractions(whatsappWebhookService);
    }

    private static String payloadComBotao(String idBotao) {
        return """
                {
                  "object": "whatsapp_business_account",
                  "entry": [{
                    "id": "waba-1",
                    "changes": [{
                      "field": "messages",
                      "value": {
                        "metadata": { "phone_number_id": "phone-123" },
                        "messages": [{
                          "from": "5511999999999",
                          "type": "interactive",
                          "interactive": {
                            "type": "button_reply",
                            "button_reply": { "id": "%s", "title": "x" }
                          }
                        }]
                      }
                    }]
                  }]
                }
                """.formatted(idBotao);
    }

    private static String assinar(String corpo) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SEGREDO.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] assinatura = mac.doFinal(corpo.getBytes(StandardCharsets.UTF_8));
        return "sha256=" + HexFormat.of().formatHex(assinatura);
    }
}
