ALTER TABLE agendamentos ADD COLUMN confirmado BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE agendamentos ADD COLUMN lembrete_enviado_em TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_agendamentos_lembrete_enviado_em ON agendamentos(lembrete_enviado_em);
