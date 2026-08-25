package com.agendaclientes.api.shared.security;

public interface TokenCipher {

    String encrypt(String texto);

    String decrypt(String textoCifrado);
}
