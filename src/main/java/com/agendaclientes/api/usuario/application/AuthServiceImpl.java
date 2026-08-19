package com.agendaclientes.api.usuario.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.agendaclientes.api.usuario.domain.Usuario;
import com.agendaclientes.api.usuario.domain.UsuarioRepository;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResult registrar(String nome, String email, String senha) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalStateException("Já existe uma conta com esse email");
        }
        Usuario usuario = Usuario.novo(nome, email, passwordEncoder.encode(senha));
        Usuario salvo = usuarioRepository.save(usuario);
        return new AuthResult(jwtService.gerarToken(salvo.getId(), salvo.getEmail()), salvo);
    }

    @Override
    public AuthResult login(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new CredenciaisInvalidasException("Email ou senha inválidos"));
        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException("Email ou senha inválidos");
        }
        return new AuthResult(jwtService.gerarToken(usuario.getId(), usuario.getEmail()), usuario);
    }

    @Override
    public String esqueciSenha(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) {
            // Não revela se o email existe ou não.
            return null;
        }
        String token = UUID.randomUUID().toString();
        usuario.definirTokenReset(token, Instant.now().plus(1, ChronoUnit.HOURS));
        usuarioRepository.save(usuario);
        return token;
    }

    @Override
    public void redefinirSenha(String email, String token, String novaSenha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new CredenciaisInvalidasException("Link de redefinição inválido ou expirado"));
        if (!usuario.tokenResetValido(token)) {
            throw new CredenciaisInvalidasException("Link de redefinição inválido ou expirado");
        }
        usuario.redefinirSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }
}
