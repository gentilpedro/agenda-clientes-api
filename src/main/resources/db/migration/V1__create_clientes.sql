CREATE TABLE clientes (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    telefone VARCHAR(50) NOT NULL,
    observacoes VARCHAR(1000),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL
);
