package com.agendaclientes.api.cliente.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.agendaclientes.api.cliente.application.ClienteService;
import com.agendaclientes.api.cliente.domain.Cliente;
import com.agendaclientes.api.shared.exception.ResourceNotFoundException;
import com.agendaclientes.api.usuario.application.JwtService;

/**
 * A fatia @WebMvcTest não carrega a SecurityConfig real (ela exige uma
 * HttpSecurity só disponível com a autoconfiguração completa de segurança),
 * então os filtros são desligados e o @AuthenticationPrincipal é resolvido
 * manualmente via um WebMvcConfigurer de teste + SecurityContextHolder.
 * JwtService ainda é importado porque JwtAuthFilter (um Filter, sempre
 * incluído nessas fatias) depende dele para ser instanciado no contexto.
 */
@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ ClienteWebMapper.class, JwtService.class, ClienteControllerTest.AuthenticationPrincipalTestConfig.class })
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void autenticarUsuario() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(usuarioId, null, List.of()));
    }

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listar_deveRetornarClientesDoUsuarioAutenticado() throws Exception {
        Cliente cliente = Cliente.existente(UUID.randomUUID(), usuarioId, "Maria", "maria@email.com", "111", null,
                Instant.now());
        when(clienteService.listarTodos(usuarioId)).thenReturn(List.of(cliente));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Maria"));
    }

    @Test
    void buscarPorId_deveRetornar404QuandoClienteNaoEncontrado() throws Exception {
        UUID id = UUID.randomUUID();
        when(clienteService.buscarPorId(usuarioId, id))
                .thenThrow(new ResourceNotFoundException("Cliente não encontrado: " + id));

        mockMvc.perform(get("/api/clientes/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_deveRetornar201ComClienteCriado() throws Exception {
        when(clienteService.criar(eq(usuarioId), any(), any(), any(), any())).thenAnswer(invocation -> Cliente.novo(
                usuarioId, invocation.getArgument(1), invocation.getArgument(2), invocation.getArgument(3),
                invocation.getArgument(4)));

        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Maria","email":"maria@email.com","telefone":"11999999999"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Maria"));
    }

    @Test
    void criar_deveRetornar400QuandoNomeEmBranco() throws Exception {
        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"","email":"maria@email.com","telefone":"11999999999"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nome").exists());
    }

    @Test
    void remover_deveRetornar204QuandoClienteRemovido() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/clientes/{id}", id))
                .andExpect(status().isNoContent());
    }

    @TestConfiguration
    static class AuthenticationPrincipalTestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }
}
