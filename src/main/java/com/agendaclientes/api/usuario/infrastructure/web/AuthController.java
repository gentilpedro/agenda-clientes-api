package com.agendaclientes.api.usuario.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agendaclientes.api.usuario.application.AuthResult;
import com.agendaclientes.api.usuario.application.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistrarRequest request) {
        AuthResult resultado = authService.registrar(request.nome(), request.email(), request.senha());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(resultado));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return toResponse(authService.login(request.email(), request.senha()));
    }

    @PostMapping("/esqueci-senha")
    public EsqueciSenhaResponse esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        String token = authService.esqueciSenha(request.email());
        return new EsqueciSenhaResponse(
                "Se o email existir, um link de redefinição foi gerado.",
                token);
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        authService.redefinirSenha(request.email(), request.token(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }

    private AuthResponse toResponse(AuthResult resultado) {
        return new AuthResponse(
                resultado.token(),
                resultado.usuario().getId(),
                resultado.usuario().getNome(),
                resultado.usuario().getEmail());
    }
}
