CREATE TABLE processamento_videos (
                                      id UUID PRIMARY KEY,
                                      nome_original VARCHAR(255) NOT NULL,
                                      path_final VARCHAR(255),
                                      email_destino VARCHAR(255) NOT NULL,
                                      nome_usuario VARCHAR(255) NOT NULL,
                                      status VARCHAR(50) NOT NULL,
                                      mensagem_erro TEXT,
                                      data_criacao TIMESTAMP NOT NULL,
                                      data_conclusao TIMESTAMP
);