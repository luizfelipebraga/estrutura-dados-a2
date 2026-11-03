# A2 — Fórmulas de Produto com Árvore Genérica

Sistema para cadastrar a fórmula (estrutura) de produtos usando uma **árvore genérica**. Cada produto é a raiz da árvore e cada componente é um nó filho, com quantidade e valor unitário.

## Sumário

- [Requisitos do trabalho](#requisitos-do-trabalho)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Como rodar o projeto](#como-rodar-o-projeto)
- [Endereços e acessos](#endereços-e-acessos)
- [Frontend: dados mockados x backend real](#frontend-dados-mockados-x-backend-real)
- [Rodando o backend fora do Docker](#rodando-o-backend-fora-do-docker)
- [Estrutura de pastas](#estrutura-de-pastas)
- [Regras de negócio](#regras-de-negócio)
- [Contrato da API](#contrato-da-api)
- [Divisão do trabalho](#divisão-do-trabalho)
- [Comandos úteis](#comandos-úteis)
- [Problemas comuns](#problemas-comuns)

## Requisitos do trabalho

O sistema deve:

1. Permitir **cadastrar a fórmula** de um produto.
2. Exibir os **detalhes do produto (explosão)**.
3. Informar a **quantidade de produtos a produzir** e responder a quantidade de componentes e o valor de aquisição.
4. **Atualizar o valor** de um componente.
5. **Remontar o custo** do produto após a atualização dos preços (**implosão**).

Os dois casos de teste do enunciado (Caneta e Computador) já vêm cadastrados como exemplo.

## Tecnologias

| Parte | Tecnologia |
|---|---|
| Backend | Java 17, Spring Boot 4.1, Spring Data JPA, Flyway, Lombok, Maven |
| Banco de dados | PostgreSQL 17 |
| Administração do banco | pgAdmin 4 |
| Frontend | React 19, TypeScript, Vite 8, React Router |
| Infraestrutura | Docker e Docker Compose |

## Pré-requisitos

Instale antes de começar:

| Ferramenta | Versão | Para quê | Como verificar |
|---|---|---|---|
| [Docker](https://docs.docker.com/get-docker/) | 24 ou superior | Rodar Postgres, pgAdmin e backend | `docker --version` |
| Docker Compose | v2 (vem com o Docker) | Subir todos os containers juntos | `docker compose version` |
| [Node.js](https://nodejs.org/) | 22 LTS ou superior (mínimo 20.19) | Rodar o frontend | `node -v` |
| npm | vem com o Node | Instalar dependências do frontend | `npm -v` |
| Java (JDK) | 17 ou superior | **Opcional**: só para rodar o backend fora do Docker | `java -version` |

> Não é preciso instalar o Maven: o projeto usa o Maven Wrapper (`./mvnw`), que baixa a versão certa sozinho.

**Windows:** use o [Docker Desktop](https://www.docker.com/products/docker-desktop/) com WSL 2. Nos comandos abaixo, troque `./mvnw` por `mvnw.cmd` e `cp` por `copy`.

**Linux:** para usar o Docker sem `sudo`, adicione seu usuário ao grupo: `sudo usermod -aG docker $USER` (depois saia e entre na sessão de novo).

## Como rodar o projeto

### 1. Entre na pasta do projeto

```bash
cd a2
```

### 2. Crie os arquivos de configuração

Copie os arquivos de exemplo. Os valores padrão já funcionam, não é preciso alterar nada.

```bash
cp .env.example .env
cp frontend/.env.example frontend/.env
```

O `.env` da raiz tem as credenciais do banco e do pgAdmin:

```env
DB_NAME=a2
DB_USERNAME=a2
DB_PASSWORD=a2
PGADMIN_EMAIL=admin@a2.com
PGADMIN_PASSWORD=admin
```

> O `PGADMIN_EMAIL` precisa ser um e-mail válido (com `@` e domínio). Se não for, o pgAdmin não inicia.

### 3. Suba o banco, o pgAdmin e o backend

```bash
docker compose up -d --build
```

Na primeira vez demora alguns minutos, porque o Docker baixa as imagens e o Maven baixa as dependências do backend. Para acompanhar:

```bash
docker compose ps              # todos devem aparecer como "running" / "healthy"
docker compose logs -f backend # espere a mensagem "Started BackendApplication"
```

Para testar se o backend está no ar:

```bash
curl http://localhost:8080/api/health
```

A resposta deve ter `"status":"UP"` e a versão do PostgreSQL.

### 4. Rode o frontend

Em outro terminal:

```bash
cd frontend
npm install
npm run dev
```

Abra http://localhost:5173 no navegador.

## Endereços e acessos

| Serviço | Endereço | Acesso |
|---|---|---|
| Frontend | http://localhost:5173 | — |
| Backend (API) | http://localhost:8080/api | — |
| Health check | http://localhost:8080/api/health | — |
| pgAdmin | http://localhost:5050 | Sem login |
| PostgreSQL | `localhost:5432` | banco `a2`, usuário `a2`, senha `a2` |

### Usando o pgAdmin

1. Acesse http://localhost:5050. Na primeira vez leva uns 30 segundos para abrir.
2. No menu da esquerda, abra **Servers → A2 Postgres**. O servidor já vem configurado.
3. Quando pedir a senha, digite `a2` e marque **Save password**.
4. As tabelas ficam em **A2 Postgres → Databases → a2 → Schemas → public → Tables**.

> A conexão pré-configurada fica em `pgadmin/servers.json` e só é lida na **primeira** inicialização do pgAdmin. Se você mudar o nome ou o usuário do banco no `.env`, ajuste esse arquivo e rode `docker compose down -v` (isso apaga os dados do banco também).

Para conectar por outro cliente (DBeaver, IntelliJ, etc.), use host `localhost`, porta `5432`, banco `a2`, usuário `a2` e senha `a2`.

## Frontend: dados mockados x backend real

Enquanto o backend não estiver pronto, o frontend funciona sozinho com **dados mockados**. Ele simula a API no próprio navegador e guarda os dados no `localStorage`, já com a Caneta e o Computador cadastrados.

Isso é controlado pelo arquivo `frontend/.env`:

```env
VITE_USE_MOCK=true   # true = dados mockados | false = usa o backend real
VITE_API_URL=/api
```

- Com `VITE_USE_MOCK=true`, aparece o selo **"Dados mockados"** no topo da página. O botão **Restaurar exemplos**, na lista de produtos, apaga tudo e recarrega os dois exemplos.
- Com `VITE_USE_MOCK=false`, o frontend chama `/api/...`. O Vite repassa essas chamadas para `http://localhost:8080` (configurado em `frontend/vite.config.ts`).

Depois de alterar o `.env`, pare o `npm run dev` (Ctrl+C) e rode de novo.

## Rodando o backend fora do Docker

Útil para desenvolver no backend com reinício automático (DevTools) e debug pela IDE. Requer Java 17.

```bash
# 1. Pare o backend do Docker, se estiver rodando
docker compose stop backend

# 2. Suba só o banco (e o pgAdmin, se quiser)
docker compose up -d postgres pgadmin

# 3. Rode o backend
cd backend
./mvnw spring-boot:run
```

Por padrão o backend se conecta em `jdbc:postgresql://localhost:5432/a2` com usuário e senha `a2`. Para mudar, defina as variáveis `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` (veja `backend/src/main/resources/application.yml`).

Na IDE, basta abrir a pasta `backend` como projeto Maven e rodar a classe `BackendApplication`.

### Migrations do banco (Flyway)

As tabelas **não** são criadas pelo Hibernate. Elas são criadas por scripts SQL em `backend/src/main/resources/db/migration`, executados automaticamente quando o backend inicia.

- O nome segue o padrão `V<número>__<descricao>.sql`, por exemplo `V1__cria_tabelas_produto.sql` e `V2__insere_exemplos.sql`.
- **Nunca altere uma migration que já rodou.** Para mudar algo, crie uma nova (`V3__...`).
- Se precisar recomeçar o banco do zero: `docker compose down -v && docker compose up -d --build`.

## Banco na nuvem (Neon)

O banco de produção fica no [Neon](https://neon.com) (projeto `purple-tooth-74852100`, branch `production`). O backend detecta sozinho as variáveis do Neon:

| Variável | Uso |
|---|---|
| `DATABASE_URL` | Conexão **pooled** (host com `-pooler`), usada pela aplicação |
| `DATABASE_URL_UNPOOLED` | Conexão **direta**, usada pelo Flyway nas migrations |

Quando `DATABASE_URL` existe, ela tem prioridade sobre `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`. A conversão do formato do Neon (`postgresql://usuario:senha@host/banco`) para JDBC fica em `backend/.../config/NeonDatabaseUrlEnvironmentPostProcessor.java`. O Docker Compose não repassa essas variáveis ao container, então `docker compose up` continua usando o Postgres local.

Para obter as variáveis (é preciso ter acesso ao projeto no Neon):

```bash
npm i -g neon
neon login
neon env pull        # grava DATABASE_URL e DATABASE_URL_UNPOOLED no .env da raiz
```

Para rodar o backend local apontando para o Neon:

```bash
cd backend
set -a; . ../.env; set +a
./mvnw spring-boot:run
```

Em produção (Render, por exemplo), cadastre `DATABASE_URL` e `DATABASE_URL_UNPOOLED` nas variáveis de ambiente do serviço. O Flyway cria as tabelas quando o backend inicia.

## Estrutura de pastas

```
a2/
├── docker-compose.yml          # Postgres, pgAdmin e backend
├── .env.example                # modelo das variáveis do Docker
├── pgadmin/servers.json        # conexão pré-configurada do pgAdmin
│
├── backend/                    # Spring Boot
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/a2/backend/
│       │   ├── BackendApplication.java
│       │   ├── config/CorsConfig.java          # libera o frontend (localhost:5173)
│       │   └── controller/HealthController.java
│       └── resources/
│           ├── application.yml                 # configuração (lê variáveis de ambiente)
│           └── db/migration/                   # scripts SQL do Flyway
│
└── frontend/                   # React + Vite
    ├── .env.example
    ├── vite.config.ts          # proxy /api -> localhost:8080
    └── src/
        ├── App.tsx             # layout e rotas
        ├── components/         # componentes genéricos (carregando, erro)
        ├── lib/                # http, formatação, hook useRequest
        └── features/produtos/
            ├── api.ts          # escolhe entre mock e API real
            ├── types.ts        # tipos do contrato da API
            ├── exemplos.ts     # Caneta e Computador (testes do enunciado)
            ├── custos.ts       # verificação de custos desatualizados
            ├── mock/           # API falsa + árvore genérica em TypeScript
            ├── components/     # árvore, abas Explosão / Produção / Preços
            └── pages/          # lista, cadastro e detalhe do produto
```

## Regras de negócio

Todos devem seguir estas regras para o backend e o frontend darem o mesmo resultado:

1. **Quantidade é por unidade do pai.** A Memória RAM com quantidade 2 dentro da Placa-Mãe significa 2 pentes por computador.
2. **O valor de um componente básico (folha) é o preço unitário de compra.**
3. **O valor de um item composto é a soma de (valor × quantidade) dos filhos.** Na Caneta: Tinta = 0,30 + 0,50 = 0,80, e Caneta = 0,80 + 1,00 + 0,70 = 2,50.
4. **Implosão** recalcula os valores de baixo para cima e corrige os itens compostos. No Computador, os valores cadastrados estão inconsistentes: Placa-Mãe vale 500, mas os filhos somam 1550. Depois da implosão o Computador passa de R$ 2.500 para **R$ 3.380**.
5. **Nomes podem se repetir.** "Plástico" aparece no Corpo (R$ 0,70) e na Tampa (R$ 0,50). Por isso:
   - Um item usado como **pai** precisa ter nome único na fórmula.
   - A atualização de preço é feita pelo **id** do item, não pelo nome.
   - No cálculo de produção, componentes com o mesmo nome são **somados**, e o valor unitário mostrado é a média ponderada.

### Resultados esperados (use como teste)

| Caso | Resultado esperado |
|---|---|
| Caneta: valor total | R$ 2,50 |
| Caneta: produzir 10 | Pigmento 10, Solvente 10, Plástico 20, Clip 10, Vedação 10. Total R$ 25,00 |
| Computador: depois da implosão | Placa-Mãe 1.550, Armazenamento 650, Gabinete 2.500, Monitor 700, Periféricos 180, Computador **3.380** |

## Contrato da API

O frontend já está pronto para estes endpoints. O backend deve seguir exatamente estes formatos.

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/produtos` | Lista os produtos |
| `POST` | `/api/produtos` | Cadastra um produto com a fórmula |
| `GET` | `/api/produtos/{id}/explosao` | Árvore completa do produto |
| `GET` | `/api/produtos/{id}/necessidade?quantidade=10` | Componentes e custo para produzir N unidades |
| `PATCH` | `/api/produtos/{id}/itens/{itemId}` | Atualiza o valor de um item |
| `POST` | `/api/produtos/{id}/implosao` | Recalcula os custos e salva |

<details>
<summary>Exemplos de requisição e resposta</summary>

**`GET /api/produtos`**

```json
[{ "id": 1, "nome": "Caneta", "quantidadeItens": 10, "valorTotal": 2.5 }]
```

**`POST /api/produtos`**: o item raiz tem `pai: null`. Retorna o produto no mesmo formato da listagem.

```json
{
  "nome": "Caneta",
  "itens": [
    { "nome": "Caneta", "pai": null, "quantidade": 1, "valor": 2.5 },
    { "nome": "Tinta", "pai": "Caneta", "quantidade": 1, "valor": 0.8 },
    { "nome": "Pigmento", "pai": "Tinta", "quantidade": 1, "valor": 0.3 }
  ]
}
```

**`GET /api/produtos/{id}/explosao`**: `valorTotal` de cada nó é `valorUnitario × quantidade`.

```json
{
  "id": 1, "nome": "Caneta", "quantidade": 1, "valorUnitario": 2.5, "valorTotal": 2.5,
  "filhos": [
    { "id": 2, "nome": "Tinta", "quantidade": 1, "valorUnitario": 0.8, "valorTotal": 0.8, "filhos": [] }
  ]
}
```

**`GET /api/produtos/{id}/necessidade?quantidade=10`**

```json
{
  "quantidade": 10,
  "componentes": [
    { "nome": "Plástico", "quantidade": 20, "valorUnitario": 0.6, "valorTotal": 12.0 }
  ],
  "valorTotal": 25.0
}
```

**`PATCH /api/produtos/{id}/itens/{itemId}`**: corpo `{ "valor": 0.45 }`. Retorna a árvore atualizada, no mesmo formato da explosão.

**`POST /api/produtos/{id}/implosao`**

```json
{
  "arvore": { "id": 1, "nome": "Computador", "...": "mesmo formato da explosão" },
  "alteracoes": [
    { "id": 3, "nome": "Placa-Mãe", "valorAntigo": 500.0, "valorNovo": 1550.0 }
  ]
}
```

**Erros** devem retornar status 400 (dados inválidos) ou 404 (não encontrado), com corpo `{ "message": "Descrição do erro" }`. O frontend mostra essa mensagem para o usuário.

</details>

## Divisão do trabalho

| Parte | Peso | Responsabilidade | Onde fica |
|---|---|---|---|
| 1. Árvore genérica e algoritmos | 40% | `TreeNode<T>`, `buildTree`, explosão, cálculo de necessidade, atualização de valor, implosão e testes JUnit com os dois exemplos | `backend/.../arvore` (Java puro, sem Spring) |
| 2. Frontend | 40% | Telas de cadastro, lista, explosão, produção e preços/implosão | `frontend/` (já implementado com dados mockados) |
| 3. Banco e API | 20% | Migrations, entidades JPA, repositórios, serviços, controllers, tratamento de erros e dados de exemplo | `backend/.../produto` + `db/migration` |

A árvore deve ser implementada **à mão**, sem bibliotecas de árvore. A versão em TypeScript em `frontend/src/features/produtos/mock/arvore.ts` serve de referência para a versão em Java.

## Comandos úteis

| Comando | O que faz |
|---|---|
| `docker compose up -d --build` | Sobe tudo, reconstruindo o backend |
| `docker compose ps` | Mostra o estado dos containers |
| `docker compose logs -f backend` | Acompanha os logs do backend |
| `docker compose stop` | Para os containers (mantém os dados) |
| `docker compose down` | Remove os containers (mantém os dados) |
| `docker compose down -v` | Remove containers **e apaga o banco** |
| `cd frontend && npm run dev` | Frontend em modo desenvolvimento |
| `cd frontend && npm run build` | Verifica tipos e gera o build de produção |
| `cd frontend && npm run lint` | Roda o linter |
| `cd backend && ./mvnw test` | Roda os testes do backend (precisa do Postgres rodando) |

## Problemas comuns

**`port is already allocated` ao subir o Docker**
Já existe algo usando a porta (5432, 5050 ou 8080), geralmente um PostgreSQL instalado na máquina. Pare esse serviço (`sudo systemctl stop postgresql` no Linux) ou mude a porta da esquerda no `docker-compose.yml`, por exemplo `"5433:5432"`.

**pgAdmin não abre em `localhost:5050`**
Rode `docker compose logs pgadmin`. Se aparecer `does not appear to be a valid email address`, corrija o `PGADMIN_EMAIL` no `.env` e rode `docker compose up -d pgadmin`. Lembre que ele leva uns 30 segundos para iniciar.

**`error getting credentials` ao baixar imagens (Linux)**
O gerenciador de credenciais do Docker está com problema. As imagens usadas são públicas, então você pode remover a linha `"credsStore"` do arquivo `~/.docker/config.json` e tentar de novo.

**Backend não sobe (`Connection refused` para o banco)**
Confira se o Postgres está `healthy` com `docker compose ps`. Se você rodou o backend fora do Docker, confira se a porta 5432 está liberada e se o `.env` tem as mesmas credenciais.

**Frontend mostra erro em vermelho (`Failed to fetch`, `500`, `502`) ao carregar os produtos**
Com `VITE_USE_MOCK=false`, o backend precisa estar rodando na porta 8080. Teste com `curl http://localhost:8080/api/health`.

**`npm run dev` fecha com `ECONNREFUSED`**
Alguma extensão do editor que se conecta ao Vite (como o Console Ninja) pode causar isso. Desative a extensão ou rode o comando num terminal fora do editor.

**`./mvnw: Permission denied`**
Rode `chmod +x backend/mvnw`.
