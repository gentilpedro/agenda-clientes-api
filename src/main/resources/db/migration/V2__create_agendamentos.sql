CREATE TABLE agendamentos (
    id UUID PRIMARY KEY,
    cliente_id UUID NOT NULL REFERENCES clientes(id),
    data_hora TIMESTAMP WITH TIME ZONE NOT NULL,
    duracao_minutos INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    observacoes VARCHAR(1000)
);

CREATE INDEX idx_agendamentos_cliente_id ON agendamentos(cliente_id);
CREATE INDEX idx_agendamentos_data_hora ON agendamentos(data_hora);
