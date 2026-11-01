# Desafio Técnico: To-Do List

Aplicação full stack para gerenciar tarefas: cadastro, edição, conclusão, exclusão, listagem com busca, filtros, ordenação e paginação no servidor, e um dashboard com os totais por status.

- **Backend:** Java 25, Spring Boot 4.1 e PostgreSQL 18, com arquitetura em camadas.
- **Frontend:** Angular 22 com Angular Material.
- **Infraestrutura:** Docker Compose sobe banco, API e interface com um único comando.

## Sumário

1. [Funcionalidades](#funcionalidades)
2. [Tecnologias](#tecnologias)
3. [Como rodar com Docker (recomendado)](#como-rodar-com-docker-recomendado)
4. [Como rodar sem Docker (desenvolvimento)](#como-rodar-sem-docker-desenvolvimento)
5. [Variáveis de ambiente](#variáveis-de-ambiente)
6. [Como rodar os testes](#como-rodar-os-testes)
7. [Arquitetura](#arquitetura)
8. [API REST](#api-rest)
9. [Exemplos de chamadas](#exemplos-de-chamadas)
10. [Decisões técnicas](#decisões-técnicas)
11. [Estrutura do repositório](#estrutura-do-repositório)
12. [Licença](#licença)

## Funcionalidades

**Tarefas**
- Criar, consultar, editar e excluir tarefas, com título, descrição, prioridade, status e data de vencimento.
- Concluir (`DONE`) e reabrir (`PENDENT`) com uma ação só.
- Exclusão com diálogo de confirmação: cancelar, apertar Esc ou clicar fora não exclui nada.

**Listagem**
- Busca por parte do título, sem diferenciar maiúsculas de minúsculas.
- Filtros por status e por prioridade, que podem ser combinados com a busca.
- Ordenação por título, vencimento ou data de criação, crescente ou decrescente.
- Paginação no servidor, com 5, 10, 25 ou 50 itens por página.
- Os filtros podem vir pela URL (`/tasks?status=DONE`).

**Dashboard**
- Total de tarefas e um card por status, com quantidade e percentual.
- Os cards abrem a listagem já filtrada.
- Barra de progresso com o percentual de tarefas concluídas.

**Experiência de uso**
- Estados de carregando, erro (com "Tentar de novo") e vazio em todas as telas.
- Snackbar confirmando cada operação.
- Mensagens de validação do servidor exibidas no campo do formulário.
- Layout para celular: até 600px, a listagem mostra 3 colunas e as ações viram um menu.
- Interface, datas e paginador em português (pt-BR).

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem do backend | Java 25 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Validation) |
| Banco de dados | PostgreSQL 18 |
| Migrations | Flyway |
| Documentação da API | springdoc-openapi 3 (Swagger UI) |
| Testes do backend | JUnit 5, Mockito, Cucumber 8 (BDD em português), Testcontainers |
| Frontend | Angular 22, Angular Material, RxJS, Signals |
| Testes do frontend | Vitest 5 (via `ng test`), com cobertura pelo `@vitest/coverage-v8` |
| Servidor web | nginx 1.29, que serve o Angular e faz o proxy de `/api` |
| Infraestrutura | Docker (imagens multi-stage) e Docker Compose |

## Como rodar com Docker (recomendado)

**Pré-requisito:** Docker com Docker Compose (Docker Desktop no Windows e no macOS). Não é preciso ter Java, Node nem PostgreSQL instalados.

```bash
git clone https://github.com/AdlerMrF/Desafio-Tecnico-To-Do-List.git
cd Desafio-Tecnico-To-Do-List

cp .env.example .env        # no Windows (cmd): copy .env.example .env
docker compose up --build
```

A primeira execução leva alguns minutos, porque baixa as dependências do Maven e do npm. Quando o log mostrar `Started TodolistApplication`, acesse:

| O quê | Endereço |
|---|---|
| Aplicação | http://localhost:4200 |
| Swagger (documentação interativa da API) | http://localhost:8080/swagger-ui.html |
| API | http://localhost:8080/api |

Comandos úteis:

```bash
docker compose up -d --build   # sobe em segundo plano
docker compose ps              # 3 containers rodando, com o banco "healthy"
docker compose logs -f backend # acompanha o log da API
docker compose down            # para tudo e mantém os dados
docker compose down -v         # para tudo e apaga o banco
```

> As portas 4200, 8080 e 5432 precisam estar livres. Se alguma estiver ocupada, troque `FRONTEND_PORT`, `BACKEND_PORT` ou `DB_PORT` no `.env`.

Sem o arquivo `.env`, o Compose para logo no início com a mensagem "Crie o arquivo .env a partir do .env.example", em vez de subir o banco sem senha.

## Como rodar sem Docker (desenvolvimento)

Para desenvolver com recarga automática:

**Pré-requisitos:** Java 25, Node.js 24 (ou outra versão aceita pelo Angular 22) e Docker, que continua sendo usado só para o banco.

1. **Banco de dados** (na raiz):
   ```bash
   cp .env.example .env
   docker compose up -d postgres
   ```
2. **Backend** (em `backend/`):
   ```bash
   ./mvnw spring-boot:run      # no Windows: mvnw spring-boot:run
   ```
   O backend lê o `../.env` automaticamente e sobe em http://localhost:8080. O Flyway cria as tabelas na primeira execução.
3. **Frontend** (em `frontend/`):
   ```bash
   npm ci
   npm start
   ```
   A aplicação abre em http://localhost:4200 e chama a API direto em `http://localhost:8080/api`, liberada pelo CORS.

## Variáveis de ambiente

Todas ficam no `.env` da raiz, criado a partir do `.env.example`. O `.env` real nunca vai para o Git.

| Variável | Padrão | Para que serve |
|---|---|---|
| `DB_HOST` | `localhost` | Host do banco quando o backend roda fora do Docker. No Compose, ele é fixado em `postgres` |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `todolist` | Nome do banco |
| `DB_USER` | `todolist` | Usuário do banco |
| `DB_PASSWORD` | (obrigatória) | Senha do banco. Troque o valor de exemplo |
| `BACKEND_PORT` | `8080` | Porta publicada da API |
| `FRONTEND_PORT` | `4200` | Porta publicada da interface |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Origens aceitas pelo CORS (separe várias por vírgula) |

## Como rodar os testes

### Backend: 47 testes

```bash
cd backend
./mvnw test                  # no Windows: mvnw test
```

O Docker precisa estar rodando, porque os testes de integração usam Testcontainers e sobem um PostgreSQL 18 descartável. O banco de desenvolvimento não é tocado.

| Tipo | O que cobre |
|---|---|
| Domínio | Regras da entidade `Task`: título obrigatório e com até 150 caracteres, status inicial, concluir e reabrir |
| Casos de uso | Os 7 casos de uso, com o repositório simulado (Mockito) |
| Web | Validação dos parâmetros da listagem, tratamento de erros, CORS e geração do OpenAPI |
| BDD (Cucumber) | 17 cenários em português, de ponta a ponta, contra um PostgreSQL real: cadastro, consulta, edição, conclusão, exclusão, filtros, ordenação, paginação, dashboard e erros |

Os cenários BDD ficam em [`backend/src/test/resources/features/tarefas.feature`](backend/src/test/resources/features/tarefas.feature).

### Frontend: 42 testes em 11 arquivos

```bash
cd frontend
npm ci
npx ng test --watch=false
npx ng test --watch=false --coverage   # gera o relatório em frontend/coverage/index.html
```

Os testes cobrem as quatro telas, o `TaskService`, o interceptor de erros, o adaptador de datas pt-BR, o paginador em português, o diálogo de confirmação e o serviço de notificações. Detalhes no [README do frontend](frontend/README.md).

## Arquitetura

```
┌───────────────┐   /api/*    ┌───────────────┐    JDBC     ┌───────────────┐
│  todolist-web │ ──────────► │  todolist-api │ ──────────► │  todolist-db  │
│ nginx+Angular │   (proxy)   │  Spring Boot  │             │ PostgreSQL 18 │
│    :4200      │             │    :8080      │             │    :5432      │
└───────────────┘             └───────────────┘             └───────────────┘
```

O navegador conversa só com o nginx. As chamadas para `/api` são repassadas ao backend pela rede interna do Compose, então tudo fica na mesma origem e o CORS não entra em jogo. O CORS só é usado no desenvolvimento, com o `ng serve` na 4200 chamando a API na 8080.

### Backend em camadas

```
web  ──►  application  ──►  domain  ◄──  infrastructure
```

| Camada | Pacote | Responsabilidade |
|---|---|---|
| **domain** | `domain.model`, `domain.query`, `domain.repository`, `domain.exception` | A entidade `Task` com as regras de negócio, os enums, os objetos de consulta e a interface `TaskRepository`. Não depende de Spring nem de JPA |
| **application** | `application.usecase` | Um caso de uso por operação: criar, consultar, editar, mudar status, excluir, listar e dashboard |
| **infrastructure** | `infrastructure.persistence`, `infrastructure.config` | Implementa o `TaskRepository` com Spring Data JPA (entidade, mapper e Specifications), além da configuração de CORS e do OpenAPI |
| **web** | `web.controller`, `web.dto`, `web.response`, `web.exception` | Controllers REST, DTOs com Bean Validation, o envelope padrão de resposta e o tratamento global de erros |

O domínio define a interface do repositório, e a infraestrutura a implementa. Assim, as regras de negócio não conhecem o banco, e os casos de uso são testados com um repositório simulado.

### Frontend

| Pasta | Conteúdo |
|---|---|
| `core/` | Modelos, `TaskService`, interceptor de erros, serviços de confirmação e notificação, e a localização pt-BR (datas e paginador) |
| `features/` | As telas: dashboard, listagem, detalhe e formulário (criar e editar) |
| `shared/` | O diálogo de confirmação reutilizável |

Cada tela é carregada sob demanda (`loadComponent`), então o pacote inicial fica em torno de 670 kB.

## API REST

Base: `http://localhost:8080/api`. A documentação interativa fica no [Swagger](http://localhost:8080/swagger-ui.html).

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| `GET` | `/tasks` | Lista com busca, filtros, ordenação e paginação | 200 |
| `POST` | `/tasks` | Cria uma tarefa | 201 |
| `GET` | `/tasks/{id}` | Consulta uma tarefa pelo UUID | 200 |
| `PUT` | `/tasks/{id}` | Edita uma tarefa (todos os campos) | 200 |
| `PATCH` | `/tasks/{id}/complete` | Marca como concluída (`DONE`) | 200 |
| `PATCH` | `/tasks/{id}/reopen` | Reabre (volta para `PENDENT`) | 200 |
| `DELETE` | `/tasks/{id}` | Exclui uma tarefa | 200 |
| `GET` | `/dashboard` | Total de tarefas e quantidade por status | 200 |

### Campos da tarefa

| Campo | Tipo | Regras |
|---|---|---|
| `id` | UUID | Gerado pelo servidor. Se vier no corpo, é ignorado |
| `title` | texto | Obrigatório, até 150 caracteres. Espaços nas pontas são removidos |
| `description` | texto | Opcional |
| `priority` | `LOW`, `MEDIUM`, `HIGH` | Obrigatória |
| `status` | `PENDENT`, `ON_GOING`, `DONE` | Opcional no cadastro (padrão `PENDENT`) e obrigatório na edição |
| `dueDate` | data (`AAAA-MM-DD`) | Opcional |
| `createdAt`, `updatedAt` | data e hora (UTC) | Preenchidos pelo servidor |

### Parâmetros da listagem (`GET /tasks`)

| Parâmetro | Valores | Padrão |
|---|---|---|
| `title` | Parte do título, sem diferenciar maiúsculas de minúsculas | — |
| `status` | `PENDENT`, `ON_GOING`, `DONE` | — |
| `priority` | `LOW`, `MEDIUM`, `HIGH` | — |
| `sort` | `TITLE`, `DUE_DATE`, `CREATED_AT` | `CREATED_AT` |
| `direction` | `ASC`, `DESC` | `DESC` |
| `page` | 0 ou mais (começa em 0) | `0` |
| `size` | De 1 a 100 | `10` |

### Contrato de resposta

Todas as respostas, inclusive as de erro, usam o mesmo envelope:

```json
{
  "status": "OK",
  "code": 200,
  "message": "Tarefa encontrada",
  "payload": { },
  "timestamp": "2026-11-01T17:35:11.247Z",
  "error": false
}
```

| Situação | Código | `message` | `payload` |
|---|---|---|---|
| Campos inválidos | 400 | `Dados inválidos` | Mapa com o campo e a mensagem do erro |
| UUID malformado na URL | 400 | `Identificador inválido: informe um UUID válido` | `null` |
| JSON malformado ou enum inválido | 400 | `Corpo da requisição inválido: verifique o JSON e os valores enviados` | `null` |
| Tarefa inexistente | 404 | `Tarefa não encontrada: {id}` | `null` |
| Rota inexistente | 404 | `Recurso não encontrado` | `null` |
| Método não suportado | 405 | `Método HTTP não suportado para este recurso` | `null` |
| Erro inesperado | 500 | `Erro interno do servidor`, sem detalhes internos (o erro vai para o log) | `null` |

## Exemplos de chamadas

As respostas abaixo foram capturadas da API rodando. O UUID e as datas variam a cada execução.

**Criar uma tarefa**

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Exemplo do README","description":"Criada pelo curl","priority":"HIGH","dueDate":"2026-12-31"}'
```

```json
{
  "status": "CREATED",
  "code": 201,
  "message": "Tarefa criada com sucesso",
  "payload": {
    "id": "b1e093fa-4b9a-4236-9049-1ab140572fb2",
    "title": "Exemplo do README",
    "description": "Criada pelo curl",
    "priority": "HIGH",
    "status": "PENDENT",
    "dueDate": "2026-12-31",
    "createdAt": "2026-11-01T17:35:10.312495686Z",
    "updatedAt": "2026-11-01T17:35:10.312495686Z"
  },
  "timestamp": "2026-11-01T17:35:10.551922322Z",
  "error": false
}
```

**Listar com busca e paginação**

```bash
curl "http://localhost:8080/api/tasks?title=exemplo&status=PENDENT&sort=TITLE&direction=ASC&page=0&size=5"
```

```json
{
  "status": "OK",
  "code": 200,
  "message": "Tarefas listadas com sucesso",
  "payload": {
    "items": [
      {
        "id": "b1e093fa-4b9a-4236-9049-1ab140572fb2",
        "title": "Exemplo do README",
        "description": "Criada pelo curl",
        "priority": "HIGH",
        "status": "PENDENT",
        "dueDate": "2026-12-31",
        "createdAt": "2026-11-01T17:35:10.312496Z",
        "updatedAt": "2026-11-01T17:35:10.312496Z"
      }
    ],
    "page": 0,
    "size": 5,
    "totalItems": 1,
    "totalPages": 1
  },
  "timestamp": "2026-11-01T17:35:11.247498082Z",
  "error": false
}
```

**Concluir e reabrir**

```bash
curl -X PATCH http://localhost:8080/api/tasks/{id}/complete   # status -> DONE
curl -X PATCH http://localhost:8080/api/tasks/{id}/reopen     # status -> PENDENT
```

**Editar**

```bash
curl -X PUT http://localhost:8080/api/tasks/{id} \
  -H "Content-Type: application/json" \
  -d '{"title":"Título novo","priority":"MEDIUM","status":"ON_GOING","dueDate":null}'
```

**Excluir**

```bash
curl -X DELETE http://localhost:8080/api/tasks/{id}
```

```json
{ "status": "OK", "code": 200, "message": "Tarefa excluída com sucesso", "payload": null, "timestamp": "2026-11-01T17:35:12.086243552Z", "error": false }
```

**Dashboard**

```bash
curl http://localhost:8080/api/dashboard
```

```json
{
  "status": "OK",
  "code": 200,
  "message": "Dashboard carregado com sucesso",
  "payload": { "total": 5, "byStatus": { "PENDENT": 3, "ON_GOING": 1, "DONE": 1 } },
  "timestamp": "2026-11-01T17:35:12.211146663Z",
  "error": false
}
```

**Erro de validação**

```bash
curl -X POST http://localhost:8080/api/tasks -H "Content-Type: application/json" -d '{"title":""}'
```

```json
{
  "status": "BAD_REQUEST",
  "code": 400,
  "message": "Dados inválidos",
  "payload": { "priority": "A prioridade é obrigatória", "title": "O título é obrigatório" },
  "timestamp": "2026-11-01T17:35:11.667495387Z",
  "error": true
}
```

**Tarefa inexistente**

```json
{
  "status": "NOT_FOUND",
  "code": 404,
  "message": "Tarefa não encontrada: 00000000-0000-0000-0000-000000000000",
  "payload": null,
  "timestamp": "2026-11-01T17:35:11.835445039Z",
  "error": true
}
```

## Decisões técnicas

- **Domínio sem framework.** A entidade `Task` valida as próprias regras: título obrigatório e aparado, até 150 caracteres, prioridade e status obrigatórios. Também concentra as transições de status (`complete` vai para `DONE`, `reopen` volta para `PENDENT`). A validação dos DTOs devolve mensagens por campo, e o domínio garante a regra mesmo se outro ponto de entrada for criado.
- **UUID gerado no servidor.** O identificador nasce no domínio (`Task.create`). Um `id` enviado no corpo é ignorado, e há um cenário BDD que garante isso.
- **Schema versionado com Flyway.** O Hibernate roda com `ddl-auto: validate`: só confere se as entidades batem com o banco, e quem cria e altera tabelas são as migrations. A tabela tem `CHECK` para prioridade e status, além de índices nas colunas usadas em filtros e ordenação.
- **Busca e filtros com Specifications.** A busca por título usa `lower(title) LIKE '%termo%'`, e os caracteres `%` e `_` digitados pelo usuário são escapados e tratados como texto. Os filtros só entram na consulta quando são informados.
- **Ordenação estável.** Toda ordenação recebe o `id` como desempate. Sem isso, tarefas com o mesmo título ou a mesma data poderiam trocar de página entre uma requisição e outra.
- **Paginação no servidor**, com o tamanho limitado a 100 itens, para que a listagem continue rápida com qualquer volume de dados.
- **Contrato único de resposta.** Sucesso e erro usam o mesmo envelope (`status`, `code`, `message`, `payload`, `timestamp`, `error`). O `GlobalExceptionHandler` converte todos os erros, inclusive os do próprio Spring (rota inexistente, método não suportado, JSON inválido), e nunca expõe stack trace.
- **Dashboard com uma consulta só.** A contagem por status usa um `GROUP BY`, e os status sem tarefas aparecem com zero.
- **Frontend com Signals e componentes standalone.** O estado das telas fica em signals. O `switchMap` cancela buscas antigas quando o filtro muda, e o debounce de 400 ms evita uma chamada por tecla. Um interceptor converte qualquer erro HTTP em `ApiError` com mensagem amigável, inclusive quando o servidor está fora do ar.
- **Telas sob demanda.** Cada rota usa `loadComponent`, então o navegador baixa o código de uma tela só quando ela é aberta.
- **Docker multi-stage.** As imagens finais levam só o necessário: o JRE e o jar no backend, e o nginx com os arquivos estáticos no frontend. O backend roda com um usuário sem privilégios (`app`). O nginx faz o proxy de `/api`, o fallback do SPA (F5 em qualquer rota funciona) e o cache longo dos arquivos com hash no nome.
- **Testes de integração com banco real.** Os testes com Cucumber e Testcontainers rodam contra um PostgreSQL 18 de verdade, igual ao de produção. Assim, eles pegam diferenças de SQL que um banco em memória esconderia.

## Estrutura do repositório

```
.
├── backend/                  API Spring Boot
│   ├── src/main/java/com/adler/todolist/
│   │   ├── domain/           entidade, regras, consultas, interface do repositório
│   │   ├── application/      casos de uso
│   │   ├── infrastructure/   JPA, CORS, OpenAPI
│   │   └── web/              controllers, DTOs, respostas, erros
│   ├── src/main/resources/
│   │   ├── application.yaml
│   │   └── db/migration/     migrations do Flyway
│   ├── src/test/             testes unitários, de integração e BDD
│   └── Dockerfile
├── frontend/                 aplicação Angular
│   ├── src/app/
│   │   ├── core/             serviços, modelos, interceptor, i18n
│   │   ├── features/         dashboard e telas de tarefas
│   │   └── shared/           diálogo de confirmação
│   ├── nginx.conf
│   └── Dockerfile
├── docker-compose.yml        banco + API + interface
└── .env.example              modelo das variáveis de ambiente
```

## Licença

Distribuído sob a licença MIT. Veja o arquivo [LICENSE](LICENSE).
