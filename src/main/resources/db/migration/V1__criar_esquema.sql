-- Esquema inicial do Mini ETL Studio.

CREATE TABLE cliente (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    nome          VARCHAR(200) NOT NULL,
    email         VARCHAR(254) NOT NULL,
    telefone      VARCHAR(30)  NOT NULL,
    cidade        VARCHAR(120) NOT NULL,
    data_criacao  TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX ux_cliente_email ON cliente (email);

CREATE TABLE processamento (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    nome_arquivo    VARCHAR(255) NOT NULL,
    data_execucao   TIMESTAMP    NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    qtd_extraidos   INTEGER      NOT NULL DEFAULT 0,
    qtd_validos     INTEGER      NOT NULL DEFAULT 0,
    qtd_invalidos   INTEGER      NOT NULL DEFAULT 0,
    qtd_duplicados  INTEGER      NOT NULL DEFAULT 0,
    qtd_carregados  INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX ix_processamento_data ON processamento (data_execucao);

CREATE TABLE registro_processamento (
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    processamento_id     INTEGER      NOT NULL,
    numero_linha         INTEGER      NOT NULL,
    -- Dados originais (nunca alterados após a extração)
    orig_nome            VARCHAR(2000),
    orig_email           VARCHAR(2000),
    orig_telefone        VARCHAR(2000),
    orig_cidade          VARCHAR(2000),
    linha_bruta          VARCHAR(8000),
    -- Dados transformados (nulos quando o campo está vazio)
    trans_nome           VARCHAR(2000),
    trans_email          VARCHAR(2000),
    trans_telefone       VARCHAR(2000),
    trans_cidade         VARCHAR(2000),
    situacao             VARCHAR(20)  NOT NULL,
    resultado_validacao  VARCHAR(4000),
    mensagem             VARCHAR(4000),
    duplicado_da_linha   INTEGER,
    cliente_id           INTEGER,
    FOREIGN KEY (processamento_id) REFERENCES processamento (id) ON DELETE CASCADE,
    FOREIGN KEY (cliente_id) REFERENCES cliente (id)
);

CREATE INDEX ix_registro_proc_linha ON registro_processamento (processamento_id, numero_linha);
CREATE INDEX ix_registro_proc_situacao ON registro_processamento (processamento_id, situacao);
