package com.agendaclientes.api.shared.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cifra dados sensíveis (ex.: token de acesso do WhatsApp de cada consultório)
 * antes de persistir. A chave configurada é normalizada via SHA-256 pra
 * sempre virar uma chave AES-256 válida, independente do tamanho do texto
 * configurado em {@code app.security.encryption-key}.
 */
@Component
public class AesGcmTokenCipher implements TokenCipher {

    private static final String TRANSFORMACAO = "AES/GCM/NoPadding";
    private static final int TAMANHO_IV_BYTES = 12;
    private static final int TAMANHO_TAG_BITS = 128;

    private final SecretKeySpec chave;
    private final SecureRandom random = new SecureRandom();

    public AesGcmTokenCipher(@Value("${app.security.encryption-key}") String encryptionKey) {
        this.chave = new SecretKeySpec(sha256(encryptionKey), "AES");
    }

    @Override
    public String encrypt(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            byte[] iv = new byte[TAMANHO_IV_BYTES];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMACAO);
            cipher.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(TAMANHO_TAG_BITS, iv));
            byte[] textoCifrado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));

            byte[] resultado = new byte[iv.length + textoCifrado.length];
            System.arraycopy(iv, 0, resultado, 0, iv.length);
            System.arraycopy(textoCifrado, 0, resultado, iv.length, textoCifrado.length);
            return Base64.getEncoder().encodeToString(resultado);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível cifrar o valor", e);
        }
    }

    @Override
    public String decrypt(String textoCifrado) {
        if (textoCifrado == null) {
            return null;
        }
        try {
            byte[] dados = Base64.getDecoder().decode(textoCifrado);
            byte[] iv = new byte[TAMANHO_IV_BYTES];
            byte[] cifrado = new byte[dados.length - TAMANHO_IV_BYTES];
            System.arraycopy(dados, 0, iv, 0, TAMANHO_IV_BYTES);
            System.arraycopy(dados, TAMANHO_IV_BYTES, cifrado, 0, cifrado.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMACAO);
            cipher.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(TAMANHO_TAG_BITS, iv));
            return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível decifrar o valor", e);
        }
    }

    private static byte[] sha256(String valor) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
