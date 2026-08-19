ALTER TABLE clientes ADD COLUMN usuario_id UUID NOT NULL REFERENCES usuarios(id);
ALTER TABLE agendamentos ADD COLUMN usuario_id UUID NOT NULL REFERENCES usuarios(id);

CREATE INDEX idx_clientes_usuario_id ON clientes(usuario_id);
CREATE INDEX idx_agendamentos_usuario_id ON agendamentos(usuario_id);
