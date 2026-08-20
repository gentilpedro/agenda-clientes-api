package com.agendaclientes.api.usuario.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.agendaclientes.api.usuario.domain.Usuario;
import com.agendaclientes.api.usuario.domain.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(usuarioRepository, passwordEncoder, jwtService);
    }

    @Test
    void registrar_deveLancarExcecaoQuandoEmailJaCadastrado() {
        when(usuarioRepository.existsByEmail("maria@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar("Maria", "maria@email.com", "senha123"))
                .isInstanceOf(IllegalStateException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrar_deveCriarUsuarioComSenhaCodificadaEGerarToken() {
        when(usuarioRepository.existsByEmail("maria@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-codificado");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            return Usuario.existente(UUID.randomUUID(), usuario.getNome(), usuario.getEmail(),
                    usuario.getSenhaHash(), usuario.getCriadoEm(), null, null);
        });
        when(jwtService.gerarToken(any(UUID.class), eq("maria@email.com"))).thenReturn("token-jwt");

        AuthResult resultado = authService.registrar("Maria", "maria@email.com", "senha123");

        assertThat(resultado.token()).isEqualTo("token-jwt");
        assertThat(resultado.usuario().getSenhaHash()).isEqualTo("hash-codificado");
    }

    @Test
    void login_deveLancarExcecaoQuandoEmailNaoEncontrado() {
        when(usuarioRepository.findByEmail("desconhecido@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("desconhecido@email.com", "senha123"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void login_deveLancarExcecaoQuandoSenhaNaoConfere() {
        Usuario usuario = Usuario.existente(UUID.randomUUID(), "Maria", "maria@email.com", "hash-atual",
                Instant.now(), null, null);
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha-errada", "hash-atual")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("maria@email.com", "senha-errada"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void login_deveRetornarTokenQuandoCredenciaisValidas() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.existente(id, "Maria", "maria@email.com", "hash-atual", Instant.now(), null, null);
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "hash-atual")).thenReturn(true);
        when(jwtService.gerarToken(id, "maria@email.com")).thenReturn("token-jwt");

        AuthResult resultado = authService.login("maria@email.com", "senha123");

        assertThat(resultado.token()).isEqualTo("token-jwt");
        assertThat(resultado.usuario()).isEqualTo(usuario);
    }

    @Test
    void esqueciSenha_naoDeveRevelarQuandoEmailNaoExiste() {
        when(usuarioRepository.findByEmail("desconhecido@email.com")).thenReturn(Optional.empty());

        String token = authService.esqueciSenha("desconhecido@email.com");

        assertThat(token).isNull();
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void esqueciSenha_deveGerarESalvarTokenDeResetQuandoEmailExiste() {
        Usuario usuario = Usuario.existente(UUID.randomUUID(), "Maria", "maria@email.com", "hash", Instant.now(),
                null, null);
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));

        String token = authService.esqueciSenha("maria@email.com");

        assertThat(token).isNotBlank();
        assertThat(usuario.tokenResetValido(token)).isTrue();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void redefinirSenha_deveLancarExcecaoQuandoTokenInvalido() {
        Usuario usuario = Usuario.existente(UUID.randomUUID(), "Maria", "maria@email.com", "hash", Instant.now(),
                "token-correto", Instant.now().plus(1, ChronoUnit.HOURS));
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authService.redefinirSenha("maria@email.com", "token-errado", "novaSenha123"))
                .isInstanceOf(CredenciaisInvalidasException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void redefinirSenha_deveAtualizarSenhaQuandoTokenValido() {
        Usuario usuario = Usuario.existente(UUID.randomUUID(), "Maria", "maria@email.com", "hash-antigo",
                Instant.now(), "token-correto", Instant.now().plus(1, ChronoUnit.HOURS));
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("hash-novo");

        authService.redefinirSenha("maria@email.com", "token-correto", "novaSenha123");

        assertThat(usuario.getSenhaHash()).isEqualTo("hash-novo");
        assertThat(usuario.getResetTokenHash()).isNull();
        verify(usuarioRepository).save(usuario);
    }
}
