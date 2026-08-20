package com.agendaclientes.api.usuario.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.agendaclientes.api.usuario.application.AuthResult;
import com.agendaclientes.api.usuario.application.AuthService;
import com.agendaclientes.api.usuario.application.CredenciaisInvalidasException;
import com.agendaclientes.api.usuario.application.JwtService;
import com.agendaclientes.api.usuario.domain.Usuario;

/**
 * Endpoints de /api/auth são públicos (ver SecurityConfig); os filtros de
 * segurança são desligados aqui porque a regra de liberação em si pertence à
 * configuração de segurança, não a este controller. JwtService ainda precisa
 * ser importado porque JwtAuthFilter (um Filter, incluído por padrão em
 * fatias @WebMvcTest) depende dele para ser instanciado no contexto.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(JwtService.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void registrar_deveRetornar201ComTokenEUsuario() throws Exception {
        Usuario usuario = Usuario.existente(UUID.randomUUID(), "Maria", "maria@email.com", "hash", Instant.now(),
                null, null);
        when(authService.registrar("Maria", "maria@email.com", "senha123"))
                .thenReturn(new AuthResult("token-jwt", usuario));

        mockMvc.perform(post("/api/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Maria","email":"maria@email.com","senha":"senha123"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.email").value("maria@email.com"));
    }

    @Test
    void registrar_deveRetornar409QuandoEmailJaCadastrado() throws Exception {
        when(authService.registrar(any(), any(), any()))
                .thenThrow(new IllegalStateException("Já existe uma conta com esse email"));

        mockMvc.perform(post("/api/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Maria","email":"maria@email.com","senha":"senha123"}
                        """))
                .andExpect(status().isConflict());
    }

    @Test
    void registrar_deveRetornar400QuandoSenhaCurta() throws Exception {
        mockMvc.perform(post("/api/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Maria","email":"maria@email.com","senha":"123"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.senha").exists());
    }

    @Test
    void login_deveRetornar401QuandoCredenciaisInvalidas() throws Exception {
        when(authService.login("maria@email.com", "senha-errada"))
                .thenThrow(new CredenciaisInvalidasException("Email ou senha inválidos"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"maria@email.com","senha":"senha-errada"}
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_deveRetornar200ComTokenQuandoCredenciaisValidas() throws Exception {
        Usuario usuario = Usuario.existente(UUID.randomUUID(), "Maria", "maria@email.com", "hash", Instant.now(),
                null, null);
        when(authService.login("maria@email.com", "senha123")).thenReturn(new AuthResult("token-jwt", usuario));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"maria@email.com","senha":"senha123"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"));
    }

    @Test
    void esqueciSenha_deveRetornar200MesmoQuandoEmailNaoExiste() throws Exception {
        when(authService.esqueciSenha("desconhecido@email.com")).thenReturn(null);

        mockMvc.perform(post("/api/auth/esqueci-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"desconhecido@email.com"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenDev").doesNotExist());
    }
}
