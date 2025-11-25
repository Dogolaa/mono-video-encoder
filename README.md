# Processador de Vídeo Monolítico (O "Vilão")

Este projeto é a implementação de um sistema monolítico para processamento de vídeo. Ele foi construído propositalmente como o **"Monolito Vilão"** para um trabalho acadêmico, servindo como a base de comparação ("o antes") contra uma futura arquitetura de microsserviços ("o depois").

O sistema recebe uploads de vídeos em lote, processa-os usando **processamento de vídeo real (via JavaCV/FFmpeg)** e persiste o status em um banco de dados.

## O Propósito: Demonstrando os Gargalos

O objetivo deste "Vilão" é demonstrar na prática os problemas clássicos de uma arquitetura monolítica síncrona quando confrontada com tarefas pesadas e em lote:

1.  **Bloqueio de Usuário (Síncrono):** A requisição HTTP (`POST`) fica "presa" (em *sending...*) até que **todos** os vídeos do lote sejam processados. Se 5 vídeos levam 8 segundos cada, o usuário espera 40 segundos pela resposta.
2.  **Processamento Sequencial:** O `for` loop no `LoteProcessingService` processa um vídeo de cada vez. O Vídeo 2 só começa *após* o Vídeo 1 terminar, desperdiçando recursos.
3.  **Orquestração Rígida:** Dentro de cada vídeo, as etapas (Resize, Watermark, Transcode) também são chamadas em sequência.
4.  **Escalabilidade Ineficiente:** Se o gargalo for a transcodificação (uso de CPU), não é possível escalar apenas essa parte. Seria necessário escalar a aplicação inteira (o `.jar` todo), incluindo o `Controller` e o `EmailService`.

## Desenvolvedores

* Caio Fernando Dias
* Lucas Dogo de Souza Pezzuto
* Matheus Malvão Barbosa

---

## Stack de Tecnologias

| Área | Tecnologia | Propósito |
| :--- | :--- | :--- |
| **Core** | Java 25 & Spring Boot 3 | Framework principal da aplicação. |
| **Processamento de Vídeo** | `JavaCV` (wrapper do `FFmpeg`) | Executa transcodificação, redimensionamento e marca d'água **reais**. |
| **Persistência** | `Spring Data JPA` (Hibernate) | Mapeamento Objeto-Relacional (ORM). |
| **Banco de Dados** | `PostgreSQL` | Armazena o status de cada trabalho de processamento. |
| **Migrações de DB** | `Flyway` | Gerencia e versiona o schema do banco de dados. |
| **Notificação** | `Spring Boot Starter Mail` | Envia o e-mail de relatório ao final do lote. |
| **Ambiente** | `Docker` & `Docker Compose` | Containeriza a aplicação e o banco de dados. |

---

## Arquitetura Interna (O Monolito Organizado)

Mesmo sendo um monolito, o projeto segue o princípio de Separação de Responsabilidades (inspirado no DDD) para ser testável e organizado:

1.  **`VideoUploadController` (Camada Web):**
    * Apenas recebe a requisição HTTP (`form-data`) e os arquivos.
    * Delega *todo* o trabalho para o `LoteProcessingService`.

2.  **`LoteProcessingService` (Camada de Aplicação / "Handler"):**
    * É o "cérebro" do caso de uso.
    * Orquestra todo o fluxo em lote (o `for` loop).
    * Chama os outros serviços para executar tarefas específicas.

3.  **`VideoOrchestratorService` (Camada de Domínio):**
    * Orquestra as etapas de *um único* vídeo (Resize -> Watermark -> Transcode).

4.  **`ResizerService`, `WatermarkerService`, `TranscoderService` (Serviços de Domínio):**
    * "Workers" que contêm a lógica complexa do `JavaCV` para cada etapa.

5.  **`VideoPersistenceService` & `VideoProcessamentoRepository` (Camada de Infra/Persistência):**
    * Abstrai toda a lógica de salvar e atualizar o status no banco de dados.

---

## Como Executar

O projeto é 100% containerizado. A única dependência é o Docker.

### 1. Pré-requisitos
* [Docker](https://www.docker.com/products/docker-desktop/)
* [Docker Compose](https://docs.docker.com/compose/install/)

### 2. Arquivo de Configuração (Obrigatório)

Este projeto precisa de credenciais de e-mail e configurações de banco específicas do Docker.

1.  **Copie** o arquivo de exemplo: `src/main/resources/application-docker.yml.example`.
2.  **Cole-o** no mesmo diretório (`src/main/resources/`).
3.  **Renomeie** a cópia para `application-docker.yml`.
4.  **Abra** o novo `application-docker.yml` e preencha seu e-mail (`username`) e sua Senha de App (`password`) do Google.

*O `.gitignore` já está configurado para **nunca** enviar seu arquivo `application-docker.yml` com segredos.*

### 3. Subindo os Containers

O `docker-compose.yml` está configurado para:
* Usar a imagem `postgres:15` (para evitar conflitos de versão).
* Mapear a pasta `./videos_processados` (no seu PC) para `/app/uploads` (no container).
* Mapear seu arquivo `application-docker.yml` para dentro do container.
* Usar o `Dockerfile` que instala o **FFmpeg** (essencial para o JavaCV).

Rode na raiz do projeto:
```bash
docker-compose up --build
````

Aguarde o Flyway rodar as migrações e o Tomcat iniciar.

-----

## Como Usar (A Demonstração do "Vilão")

Use uma ferramenta de API (Postman, Insomnia) para simular o upload de um usuário.

**Endpoint:** `POST http://localhost:8082/api/v1/processar-videos`

**Body:** Selecione o tipo **`form-data`**.

| KEY | VALUE |
| :--- | :--- |
| `videos` | (Mude o tipo para `File`) -\> Selecione `video1.mp4` |
| `videos` | (Mude o tipo para `File`) -\> Selecione `video2.mp4` |
| `videos` | (Mude o tipo para `File`) -\> Selecione `video3.mp4` |
| `emailDestino` | `seu-email@gmail.com` |
| `nomeUsuario` | `Professor` |

*(Adicione a chave `videos` várias vezes para enviar um lote)*

### O "Show" (O que observar):

1.  **No Postman:** Clique em "Send". O Postman ficará "carregando" (Sending...) por um longo tempo (ex: 3 vídeos \* 8s = 24 segundos). **Este é o Bloqueio Síncrono.**
2.  **No Terminal do Docker:** Observe os logs. Você verá o `LoteProcessingService` processando os vídeos **um de cada vez, em sequência**.
3.  **Na Pasta do Projeto:** Abra a pasta `videos_processados` (que o Docker criou). Você verá os arquivos `..._FINAL.mp4` aparecendo **um por um**, assim que terminam.
4.  **No seu E-mail:** Após os 24 segundos, você receberá um *único* e-mail com o relatório do lote.
5.  **No Banco de Dados:** (Use DBeaver ou pgAdmin para conectar em `localhost:5433`) Verifique a tabela `processamento_videos`. Você verá os registros sendo atualizados de `PENDENTE` -\> `PROCESSANDO` -\> `CONCLUIDO`.
