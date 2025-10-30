# Processador de Vídeo Monolítico (Simulador)

Aplicação monolítica que simula o processamento de uploads de vídeo. Serve como base de comparação ("O Vilão") para demonstrar os gargalos de performance e resiliência de uma arquitetura síncrona.

**Nota: Este é um simulador que usa carga de CPU artificial e não um *encoder* de vídeo real.**

---

## O Problema: O Monolito Síncrono

Este sistema demonstra os problemas clássicos de uma arquitetura monolítica síncrona:

* **Bloqueio de Usuário (Lentidão):** O usuário faz o upload e precisa esperar todo o processo (Transcodificação + Análise + Notificação) terminar para receber uma resposta.
* **Processamento Sequencial:** As tarefas de transcodificação e análise, que poderiam rodar em paralelo, são executadas uma após a outra, aumentando o tempo total.
* **Falta de Resiliência:** Se o serviço de e-mail (o último passo) falhar, todo o trabalho anterior é perdido, o usuário recebe um erro e o processo precisa ser refeito.

## Stack de Tecnologias

* **Linguagem:** Java 25 (ou a versão do seu Temurin)
* **Framework:** Spring Boot 3
* **Banco de Dados:** PostgreSQL
* **Notificação:** Spring Boot Starter Mail (com Gmail SMTP)
* **Containerização:** Docker e Docker Compose
* **Build:** Maven

---

## Como Executar

O projeto é 100% containerizado. A única dependência necessária na sua máquina é o Docker.

### 1. Pré-requisitos

* [Docker](https://www.docker.com/products/docker-desktop/)
* [Docker Compose](https://docs.docker.com/compose/install/) (geralmente já vem com o Docker Desktop)

### 2. Arquivo de Configuração (Importante!)

Este projeto precisa de credenciais de e-mail para funcionar e de configurações de banco de dados específicas para o ambiente Docker. Por motivos de segurança, o arquivo de configuração real (`application-docker.yml`) não está no repositório.

Você precisa criá-lo a partir do exemplo fornecido:

1.  **Copie** o arquivo de exemplo `src/main/resources/application-docker.yml.example`.
2.  **Cole-o** no mesmo diretório (`src/main/resources/`).
3.  **Renomeie** a cópia para `application-docker.yml`.
4.  **Abra** o novo `application-docker.yml` e preencha seu `username` (e-mail) e `password` (Senha de App do Google) nos locais indicados.

O arquivo `.gitignore` já está configurado para impedir que seu arquivo local com segredos (`application-docker.yml`) seja enviado ao Git.

### 3. Subindo os Containers

Com seu arquivo `application-docker.yml` devidamente preenchido, rode o seguinte comando na raiz do projeto:

```bash
docker-compose up --build