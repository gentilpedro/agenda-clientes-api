ALTER TABLE usuarios ADD COLUMN whatsapp_waba_id VARCHAR(50);
ALTER TABLE usuarios ADD COLUMN whatsapp_phone_number_id VARCHAR(50);
ALTER TABLE usuarios ADD COLUMN whatsapp_numero_exibicao VARCHAR(30);
ALTER TABLE usuarios ADD COLUMN whatsapp_token_acesso_criptografado VARCHAR(2000);
ALTER TABLE usuarios ADD COLUMN whatsapp_conectado_em TIMESTAMP WITH TIME ZONE;
ALTER TABLE usuarios ADD COLUMN whatsapp_template_status VARCHAR(20);

CREATE UNIQUE INDEX idx_usuarios_whatsapp_phone_number_id ON usuarios(whatsapp_phone_number_id);
