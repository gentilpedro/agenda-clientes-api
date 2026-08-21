package com.agendaclientes.api.usuario.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agendaclientes.api.shared.web.ApiError;
import com.agendaclientes.api.usuario.application.AuthResult;
import com.agendaclientes.api.usuario.application.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Autenticação", description = "Registro, login e redefinição de senha. Endpoints públicos.")
@ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ApiError.class)))
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Cria uma conta e já devolve o token de acesso")
    @ApiResponse(responseCode = "201", description = "Conta criada")
    @ApiResponse(responseCode = "409", description = "Já existe uma conta com esse email", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/registrar")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistrarRequest request) {
        AuthResult resultado = authService.registrar(request.nome(), request.email(), request.senha());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(resultado));
    }

    @Operation(summary = "Autentica e devolve o token de acesso",
            description = "Use o token devolvido aqui no botão **Authorize** para chamar os endpoints protegidos.")
    @ApiResponse(responseCode = "200", description = "Autenticado")
    @ApiResponse(responseCode = "401", description = "Email ou senha inválidos", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return toResponse(authService.login(request.email(), request.senha()));
    }

    @Operation(summary = "Solicita a redefinição de senha",
            description = """
                    Responde sempre 200, exista ou não a conta, para não revelar quais emails estão \
                    cadastrados. Como ainda não há envio de email configurado, o token vem no campo \
                    `tokenDev` da resposta quando a conta existe.""")
    @ApiResponse(responseCode = "200", description = "Solicitação recebida")
    @PostMapping("/esqueci-senha")
    public EsqueciSenhaResponse esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        String token = authService.esqueciSenha(request.email());
        return new EsqueciSenhaResponse(
                "Se o email existir, um link de redefinição foi gerado.",
                token);
    }

    @Operation(summary = "Redefine a senha usando o token de redefinição")
    @ApiResponse(responseCode = "204", description = "Senha redefinida", content = @Content)
    @ApiResponse(responseCode = "401", description = "Link de redefinição inválido ou expirado", content = @Content(schema = @Schema(implementation = ApiError.class)))
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
