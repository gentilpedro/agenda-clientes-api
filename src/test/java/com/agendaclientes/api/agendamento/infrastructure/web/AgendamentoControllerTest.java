package com.agendaclientes.api.agendamento.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import com.agendaclientes.api.agendamento.application.AgendamentoService;
import com.agendaclientes.api.agendamento.domain.Agendamento;
import com.agendaclientes.api.agendamento.domain.AgendamentoStatus;
import com.agendaclientes.api.usuario.application.JwtService;

/**
 * A fatia @WebMvcTest não carrega a SecurityConfig real (ela exige uma
 * HttpSecurity só disponível com a autoconfiguração completa de segurança),
 * então os filtros são desligados e o @AuthenticationPrincipal é resolvido
 * manualmente via um WebMvcConfigurer de teste + SecurityContextHolder.
 * JwtService ainda é importado porque JwtAuthFilter (um Filter, sempre
 * incluído nessas fatias) depende dele para ser instanciado no contexto.
 */
@WebMvcTest(AgendamentoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ AgendamentoWebMapper.class, JwtService.class,
        AgendamentoControllerTest.AuthenticationPrincipalTestConfig.class })
class AgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgendamentoService agendamentoService;

    private final UUID usuarioId = UUID.randomUUID();
    private final UUID clienteId = UUID.randomUUID();

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
    void listar_semFiltroDeData_deveChamarListarTodos() throws Exception {
        Agendamento agendamento = Agendamento.existente(UUID.randomUUID(), usuarioId, clienteId, Instant.now(), 30,
                AgendamentoStatus.AGENDADO, null);
        when(agendamentoService.listarTodos(usuarioId)).thenReturn(List.of(agendamento));

        mockMvc.perform(get("/api/agendamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AGENDADO"));
    }

    @Test
    void listar_comFiltroDeData_deveChamarListarPorData() throws Exception {
        when(agendamentoService.listarPorData(eq(usuarioId), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/agendamentos").param("data", "2026-03-10"))
                .andExpect(status().isOk());
    }

    @Test
    void criar_deveRetornar400QuandoDuracaoMenorQueOMinimo() throws Exception {
        mockMvc.perform(post("/api/agendamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"clienteId":"%s","dataHora":"2026-03-10T10:00:00Z","duracaoMinutos":1}
                        """.formatted(clienteId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.duracaoMinutos").exists());
    }

    @Test
    void criar_deveRetornar201QuandoDadosValidos() throws Exception {
        when(agendamentoService.criar(eq(usuarioId), eq(clienteId), any(), eq(30), any()))
                .thenAnswer(invocation -> Agendamento.novo(usuarioId, clienteId, invocation.getArgument(2), 30,
                        invocation.getArgument(4)));

        mockMvc.perform(post("/api/agendamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"clienteId":"%s","dataHora":"2026-03-10T10:00:00Z","duracaoMinutos":30}
                        """.formatted(clienteId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADO"));
    }

    @Test
    void cancelar_deveRetornar409QuandoAgendamentoJaConcluido() throws Exception {
        UUID id = UUID.randomUUID();
        when(agendamentoService.cancelar(usuarioId, id))
                .thenThrow(new IllegalStateException("Não é possível cancelar um agendamento já concluído"));

        mockMvc.perform(patch("/api/agendamentos/{id}/cancelar", id))
                .andExpect(status().isConflict());
    }

    @TestConfiguration
    static class AuthenticationPrincipalTestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }
}
