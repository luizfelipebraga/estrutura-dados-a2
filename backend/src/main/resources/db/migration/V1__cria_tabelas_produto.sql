CREATE TABLE produto (
    id        SERIAL PRIMARY KEY,
    nome      VARCHAR(255) NOT NULL,
    criado_em TIMESTAMP DEFAULT NOW()
);

CREATE TABLE item_formula (
    id         SERIAL PRIMARY KEY,
    produto_id INTEGER       NOT NULL REFERENCES produto (id) ON DELETE CASCADE,
    pai_id     INTEGER       REFERENCES item_formula (id) ON DELETE CASCADE,
    nome       VARCHAR(255)  NOT NULL,
    quantidade INTEGER       NOT NULL CHECK (quantidade > 0),
    valor      NUMERIC(12,2) NOT NULL CHECK (valor >= 0)
);

CREATE INDEX idx_item_formula_produto ON item_formula (produto_id);
CREATE INDEX idx_item_formula_pai     ON item_formula (pai_id);
