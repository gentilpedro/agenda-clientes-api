package com.agendaclientes.api.usuario.domain;

import java.time.Instant;
import java.util.UUID;

public class Usuario {

    private final UUID id;
    private String nome;
    private final String email;
    private String senhaHash;
    private final Instant criadoEm;
    private String resetTokenHash;
    private Instant resetTokenExpiraEm;
    private String whatsappWabaId;
    private String whatsappPhoneNumberId;
    private String whatsappNumeroExibicao;
    private String whatsappTokenAcesso;
    private Instant whatsappConectadoEm;
    private String whatsappTemplateStatus;

    private Usuario(UUID id, String nome, String email, String senhaHash, Instant criadoEm,
            String resetTokenHash, Instant resetTokenExpiraEm, String whatsappWabaId, String whatsappPhoneNumberId,
            String whatsappNumeroExibicao, String whatsappTokenAcesso, Instant whatsappConectadoEm,
            String whatsappTemplateStatus) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.criadoEm = criadoEm;
        this.resetTokenHash = resetTokenHash;
        this.resetTokenExpiraEm = resetTokenExpiraEm;
        this.whatsappWabaId = whatsappWabaId;
        this.whatsappPhoneNumberId = whatsappPhoneNumberId;
        this.whatsappNumeroExibicao = whatsappNumeroExibicao;
        this.whatsappTokenAcesso = whatsappTokenAcesso;
        this.whatsappConectadoEm = whatsappConectadoEm;
        this.whatsappTemplateStatus = whatsappTemplateStatus;
    }

    public static Usuario novo(String nome, String email, String senhaHash) {
        return new Usuario(null, nome, email, senhaHash, Instant.now(), null, null, null, null, null, null, null,
                null);
    }

    public static Usuario existente(UUID id, String nome, String email, String senhaHash, Instant criadoEm,
            String resetTokenHash, Instant resetTokenExpiraEm, String whatsappWabaId, String whatsappPhoneNumberId,
            String whatsappNumeroExibicao, String whatsappTokenAcesso, Instant whatsappConectadoEm,
            String whatsappTemplateStatus) {
        return new Usuario(id, nome, email, senhaHash, criadoEm, resetTokenHash, resetTokenExpiraEm, whatsappWabaId,
                whatsappPhoneNumberId, whatsappNumeroExibicao, whatsappTokenAcesso, whatsappConectadoEm,
                whatsappTemplateStatus);
    }

    /** Sobrecarga de conveniência pra código/testes anteriores ao WhatsApp — sem conexão nenhuma. */
    public static Usuario existente(UUID id, String nome, String email, String senhaHash, Instant criadoEm,
            String resetTokenHash, Instant resetTokenExpiraEm) {
        return existente(id, nome, email, senhaHash, criadoEm, resetTokenHash, resetTokenExpiraEm, null, null, null,
                null, null, null);
    }

    public void definirTokenReset(String tokenHash, Instant expiraEm) {
        this.resetTokenHash = tokenHash;
        this.resetTokenExpiraEm = expiraEm;
    }

    public boolean tokenResetValido(String tokenHash) {
        return resetTokenHash != null
                && resetTokenHash.equals(tokenHash)
                && resetTokenExpiraEm != null
                && resetTokenExpiraEm.isAfter(Instant.now());
    }

    public void redefinirSenha(String novaSenhaHash) {
        this.senhaHash = novaSenhaHash;
        this.resetTokenHash = null;
        this.resetTokenExpiraEm = null;
    }

    public void conectarWhatsapp(String wabaId, String phoneNumberId, String numeroExibicao, String tokenAcesso,
            Instant conectadoEm) {
        this.whatsappWabaId = wabaId;
        this.whatsappPhoneNumberId = phoneNumberId;
        this.whatsappNumeroExibicao = numeroExibicao;
        this.whatsappTokenAcesso = tokenAcesso;
        this.whatsappConectadoEm = conectadoEm;
        this.whatsappTemplateStatus = "PENDENTE";
    }

    public void desconectarWhatsapp() {
        this.whatsappWabaId = null;
        this.whatsappPhoneNumberId = null;
        this.whatsappNumeroExibicao = null;
        this.whatsappTokenAcesso = null;
        this.whatsappConectadoEm = null;
        this.whatsappTemplateStatus = null;
    }

    public void atualizarStatusTemplate(String status) {
        this.whatsappTemplateStatus = status;
    }

    public boolean whatsappConectado() {
        return whatsappPhoneNumberId != null;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public String getResetTokenHash() {
        return resetTokenHash;
    }

    public Instant getResetTokenExpiraEm() {
        return resetTokenExpiraEm;
    }

    public String getWhatsappWabaId() {
        return whatsappWabaId;
    }

    public String getWhatsappPhoneNumberId() {
        return whatsappPhoneNumberId;
    }

    public String getWhatsappNumeroExibicao() {
        return whatsappNumeroExibicao;
    }

    public String getWhatsappTokenAcesso() {
        return whatsappTokenAcesso;
    }

    public Instant getWhatsappConectadoEm() {
        return whatsappConectadoEm;
    }

    public String getWhatsappTemplateStatus() {
        return whatsappTemplateStatus;
    }
}
