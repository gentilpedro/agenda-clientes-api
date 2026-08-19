package com.agendaclientes.api.usuario.application;

public interface AuthService {

    AuthResult registrar(String nome, String email, String senha);

    AuthResult login(String email, String senha);

    String esqueciSenha(String email);

    void redefinirSenha(String email, String token, String novaSenha);
}
