package com.agendaclientes.api.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AesGcmTokenCipherTest {

    private final AesGcmTokenCipher cipher = new AesGcmTokenCipher("chave-de-teste-para-cifrar-tokens");

    @Test
    void encryptDecrypt_deveFazerRoundTrip() {
        String original = "token-de-acesso-super-secreto";

        String cifrado = cipher.encrypt(original);
        String decifrado = cipher.decrypt(cifrado);

        assertThat(decifrado).isEqualTo(original);
    }

    @Test
    void encrypt_deveProduzirTextoDiferenteDoOriginal() {
        String original = "token-de-acesso-super-secreto";

        String cifrado = cipher.encrypt(original);

        assertThat(cifrado).isNotEqualTo(original);
        assertThat(cifrado).doesNotContain(original);
    }

    @Test
    void encrypt_deveProduzirSaidasDiferentesParaAMesmaEntrada() {
        String original = "token-de-acesso-super-secreto";

        String cifrado1 = cipher.encrypt(original);
        String cifrado2 = cipher.encrypt(original);

        assertThat(cifrado1).isNotEqualTo(cifrado2);
    }

    @Test
    void encryptDecrypt_devePreservarNuloParaFacilitarUsuarioSemConexao() {
        assertThat(cipher.encrypt(null)).isNull();
        assertThat(cipher.decrypt(null)).isNull();
    }
}
