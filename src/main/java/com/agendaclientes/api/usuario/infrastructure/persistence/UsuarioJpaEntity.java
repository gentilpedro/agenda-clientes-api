package com.agendaclientes.api.usuario.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "reset_token_hash")
    private String resetTokenHash;

    @Column(name = "reset_token_expira_em")
    private Instant resetTokenExpiraEm;

    @Column(name = "whatsapp_waba_id")
    private String whatsappWabaId;

    @Column(name = "whatsapp_phone_number_id")
    private String whatsappPhoneNumberId;

    @Column(name = "whatsapp_numero_exibicao")
    private String whatsappNumeroExibicao;

    @Column(name = "whatsapp_token_acesso_criptografado")
    private String whatsappTokenAcessoCriptografado;

    @Column(name = "whatsapp_conectado_em")
    private Instant whatsappConectadoEm;

    @Column(name = "whatsapp_template_status")
    private String whatsappTemplateStatus;

    protected UsuarioJpaEntity() {
    }

    public UsuarioJpaEntity(UUID id, String nome, String email, String senhaHash, Instant criadoEm,
            String resetTokenHash, Instant resetTokenExpiraEm, String whatsappWabaId, String whatsappPhoneNumberId,
            String whatsappNumeroExibicao, String whatsappTokenAcessoCriptografado, Instant whatsappConectadoEm,
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
        this.whatsappTokenAcessoCriptografado = whatsappTokenAcessoCriptografado;
        this.whatsappConectadoEm = whatsappConectadoEm;
        this.whatsappTemplateStatus = whatsappTemplateStatus;
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

    public String getWhatsappTokenAcessoCriptografado() {
        return whatsappTokenAcessoCriptografado;
    }

    public Instant getWhatsappConectadoEm() {
        return whatsappConectadoEm;
    }

    public String getWhatsappTemplateStatus() {
        return whatsappTemplateStatus;
    }
}
