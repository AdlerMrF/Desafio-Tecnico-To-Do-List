# To-Do List: Desafio Técnico Full Stack

Aplicação web para gerenciamento de tarefas pessoais: cadastro, consulta, edição, conclusão, reabertura e exclusão, com listagem paginada, ordenada e filtrada no servidor, e um dashboard com os totais por status.

Desenvolvida para o desafio técnico de estágio Full Stack da **Place Tecnologia e Inovação**.

| Camada | Tecnologia |
|---|---|
| Backend | Java 25, Spring Boot 4.1, Spring Data JPA, Bean Validation |
| Frontend | Angular 22, Angular Material, Reactive Forms, Signals |
| Banco | PostgreSQL 18, com migrations via Flyway |
| Ambiente | Docker e Docker Compose (nginx servindo o frontend) |
| Testes | JUnit 5, Mockito, Cucumber (Gherkin em português), Testcontainers, Vitest |
| Documentação | OpenAPI 3 com Swagger UI (springdoc) |

---

## Sumário

1. [Como executar com Docker Compose](#como-executar-com-docker-compose)
2. [Como rodar os testes](#como-rodar-os-testes)
3. [Arquitetura](#arquitetura)
4. [API REST](#api-rest)
5. [Exemplos de chamadas](#exemplos-de-chamadas)
6. [Funcionalidades concluídas](#funcionalidades-concluídas)
7. [Decisões técnicas](#decisões-técnicas)
8. [Limitações conhecidas](#limitações-conhecidas)
9. [Executando sem Docker (desenvolvimento)](#executando-sem-docker-desenvolvimento)

---

## Como executar com Docker Compose

**Pré-requisito:** Docker com o Docker Compose (Docker Desktop no Windows e no macOS).

```bash
git clone https://github.com/AdlerMrF/Desafio-Tecnico-To-Do-List.git
cd Desafio-Tecnico-To-Do-List
cp .env.example .env          # no Windows (cmd): copy .env.example .env
docker compose up --build
```

O primeiro build leva alguns minutos, porque baixa as imagens e as dependências do Maven e do npm. Depois disso:

| Serviço | Endereço |
|---|---|
| Aplicação (frontend) | http://localhost:4200 |
| API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| PostgreSQL | `localhost:5432` |

**Comandos úteis:**

```bash
docker compose up -d --build   # sobe em segundo plano
docker compose ps              # situação dos containers
docker compose logs -f backend # log da API
docker compose down            # para tudo (os dados continuam no volume)
docker compose down -v         # para tudo e apaga o banco
```

### Variáveis de ambiente

As variáveis ficam no arquivo `.env`, criado a partir do `.env.example`. O `.env` não é versionado.

| Variável | Padrão | Uso |
|---|---|---|
| `DB_HOST` | `localhost` | Host do banco quando a API roda fora do Docker. No Compose, ele é sempre `postgres` |
| `DB_PORT` | `5432` | Porta do PostgreSQL publicada no computador |
| `DB_NAME` | `todolist` | Nome do banco |
| `DB_USER` | `todolist` | Usuário do banco |
| `DB_PASSWORD` | sem padrão | Senha do banco (**obrigatória**; o Compose para com uma mensagem clara se ela faltar) |
| `BACKEND_PORT` | `8080` | Porta da API publicada no computador |
| `FRONTEND_PORT` | `4200` | Porta do frontend publicada no computador |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Origens aceitas pelo CORS da API, separadas por vírgula |

### Como os containers conversam

```
Navegador ──► frontend (nginx :80, publicado em :4200)
                 ├── /           → arquivos do Angular (com fallback para index.html)
                 └── /api/*      → proxy para backend:8080
                                       │
                                       ▼
                               backend (Spring Boot :8080) ──► postgres (:5432, volume postgres_data)
```

- O nginx faz **proxy de `/api`** para o backend, então o navegador vê uma única origem e não depende de CORS.
- O backend só sobe depois que o **healthcheck** do PostgreSQL (`pg_isready`) passa; assim o Flyway nunca encontra o banco ainda iniciando.
- Os dados ficam no volume **`postgres_data`** e sobrevivem a um `docker compose down`.

---

## Como rodar os testes

### Backend

```bash
cd backend
./mvnw test        # no Windows: mvnw test
```

**Pré-requisitos:** Java 25 e o **Docker rodando**, porque os testes de integração e os cenários do Cucumber sobem um PostgreSQL 18 real com Testcontainers.

| Tipo | Ferramentas | O que cobre |
|---|---|---|
| Unitários do domínio | JUnit 5 | Regras da entidade `Task`: título obrigatório, limite de 150 caracteres, concluir e reabrir |
| Unitários dos casos de uso | JUnit 5 + Mockito | Cada caso de uso com o repositório simulado |
| Web | JUnit 5 + Spring | Tratamento de erros, CORS, documentação OpenAPI e conversão dos filtros da listagem |
| Comportamento (BDD) | Cucumber + Testcontainers | 17 cenários em Gherkin, em português, contra a API e um PostgreSQL real |

Os cenários estão em [`backend/src/test/resources/features/tarefas.feature`](backend/src/test/resources/features/tarefas.feature). Exemplo:

```gherkin
Cenário: Cadastrar tarefa com sucesso
  Dado que informo uma tarefa válida
  Quando envio a solicitação de cadastro
  Então a API deve retornar HTTP 201
  E a resposta deve conter um UUID
  E a tarefa deve estar persistida
```

O relatório HTML do Cucumber é gerado em `backend/target/cucumber-report.html`.

### Frontend

```bash
cd frontend
npm ci
npx ng test --watch=false                # 42 testes (Vitest)
npx ng test --watch=false --coverage     # com relatório de cobertura em frontend/coverage/
```

Os testes cobrem o service HTTP, o interceptor de erros, cada tela (listagem, detalhe, formulário e dashboard), o diálogo de confirmação, o adaptador de datas em pt-BR e o paginador traduzido.

---

## Arquitetura

### Backend: Onion Architecture

As dependências apontam sempre para dentro: o **domínio** não conhece Spring, HTTP nem JPA; a **aplicação** depende só do domínio; a **infraestrutura** e a **web** dependem das camadas internas, e nunca o contrário.

```
web  ─────────►  application  ─────────►  domain  ◄─────────  infrastructure
(HTTP)           (casos de uso)           (regras)            (JPA, config)
```

```
backend/src/main/java/com/adler/todolist
├── domain                      # Núcleo: Java puro, sem Spring nem JPA
│   ├── model                   # Task (regras de negócio), Priority, TaskStatus
│   ├── exception               # TaskNotFoundException, InvalidTaskException
│   ├── query                   # TaskQuery, TaskFilter, TaskSortField, SortDirection, PageResult
│   └── repository              # TaskRepository: a porta que o domínio exige
├── application
│   └── usecase                 # Create, Get, Update, ChangeTaskStatus, Delete, ListTasks, GetDashboard
├── infrastructure
│   ├── persistence             # TaskEntity, Spring Data, Specifications, adaptador do repositório
│   └── config                  # CORS e OpenAPI
└── web
    ├── controller              # TaskController e DashboardController (+ docs do Swagger)
    ├── dto                     # Requests e responses, com Bean Validation
    ├── response                # ApiResponse e ApiResponseFactory: o contrato JSON
    └── exception               # GlobalExceptionHandler: todos os erros no contrato
```

**Pontos principais:**
- **A entidade de domínio `Task` é diferente da `TaskEntity` do JPA.** O `TaskEntityMapper` converte uma na outra, e as anotações de persistência não vazam para o domínio.
- **`TaskRepository` é uma interface do domínio** (porta). A implementação (`TaskRepositoryAdapter`) fica na infraestrutura e usa Spring Data por baixo.
- **O UUID é gerado no domínio** (`UUID.randomUUID()` em `Task.create`) e gravado no tipo nativo `UUID` do PostgreSQL. Um `id` enviado no corpo da requisição é ignorado.
- **Filtros, ordenação e paginação rodam no PostgreSQL**, com JPA Specifications combináveis e `Pageable`, e nunca em memória.
- **O dashboard é calculado com uma única consulta `GROUP BY status`**; os status sem tarefas aparecem com zero.
- **O schema é versionado pelo Flyway** (`V1__create_tasks.sql`), e o Hibernate apenas valida (`ddl-auto: validate`).

### Frontend

```
frontend/src/app
├── core
│   ├── models          # Tipos do contrato JSON, Task, filtros e paginação
│   ├── services        # TaskService (HTTP), ConfirmService (MatDialog), NotificationService (snackbar)
│   ├── interceptors    # Converte qualquer erro HTTP em ApiError, num único lugar
│   └── i18n            # Paginador e datepicker em português
├── shared
│   └── confirm-dialog  # Diálogo de confirmação reutilizável
└── features
    ├── dashboard       # Cards com os totais por status e progresso
    └── tasks           # Listagem, detalhe e formulário (criação e edição)
```

- **A apresentação fica separada da comunicação HTTP.** Os componentes não usam o `HttpClient` diretamente: tudo passa pelo `TaskService`, que desembrulha o `payload` do contrato.
- **Os erros são tratados num só lugar.** O interceptor lê `error`, `message` e `payload` do contrato e entrega às telas um `ApiError`. Os erros de validação (400) aparecem embaixo do campo correspondente no formulário.
- **O estado vive em signals.** A busca é reativa (`toObservable` + `switchMap`): mudar um filtro cancela a requisição anterior.
- **Filtros, ordenação e paginação são feitos no servidor.** A `MatTable` exibe apenas a página que veio da API.
- **As rotas usam lazy loading** (`loadComponent`), e os parâmetros da rota chegam como inputs (`withComponentInputBinding`).

---

## API REST

Todas as respostas com corpo seguem o contrato JSON, com `Content-Type: application/json` e o campo `code` igual ao status HTTP:

```json
{
  "status": "OK",
  "code": 200,
  "message": "Tarefa encontrada",
  "payload": { },
  "timestamp": "2026-11-01T14:30:00.123Z",
  "error": false
}
```

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| `POST` | `/api/tasks` | Cadastra uma tarefa | 201 |
| `GET` | `/api/tasks` | Lista com filtros, ordenação e paginação | 200 |
| `GET` | `/api/tasks/{id}` | Consulta uma tarefa pelo UUID | 200 |
| `PUT` | `/api/tasks/{id}` | Edita título, descrição, prioridade, status e vencimento | 200 |
| `PATCH` | `/api/tasks/{id}/complete` | Conclui a tarefa (status `DONE`) | 200 |
| `PATCH` | `/api/tasks/{id}/reopen` | Reabre a tarefa (status `PENDENT`) | 200 |
| `DELETE` | `/api/tasks/{id}` | Exclui a tarefa | 200 |
| `GET` | `/api/dashboard` | Total de tarefas e totais por status | 200 |

**Parâmetros da listagem** (todos opcionais e combináveis):

| Parâmetro | Valores | Padrão |
|---|---|---|
| `title` | Busca parcial, sem diferenciar maiúsculas e minúsculas | |
| `status` | `PENDENT`, `ON_GOING`, `DONE` | |
| `priority` | `LOW`, `MEDIUM`, `HIGH` | |
| `sort` | `TITLE`, `CREATED_AT`, `DUE_DATE` | `CREATED_AT` |
| `direction` | `ASC`, `DESC` | `DESC` |
| `page` | A partir de `0` | `0` |
| `size` | De `1` a `100` | `10` |

**Erros:**

| Situação | HTTP | `payload` |
|---|---|---|
| Campo inválido no corpo (ex.: título vazio) | 400 | `{ "campo": "mensagem" }` |
| UUID malformado (ex.: `/api/tasks/abc`) | 400 | `null` |
| JSON malformado ou valor de enum inexistente | 400 | `null` |
| UUID válido, mas inexistente | 404 | `null` |
| Rota inexistente | 404 | `null` |
| Método não suportado | 405 | `null` |
| `Content-Type` diferente de JSON | 415 | `null` |
| Erro inesperado | 500 | `null` (o detalhe vai só para o log) |

A documentação interativa, com exemplos de cada resposta, está no **Swagger UI**: http://localhost:8080/swagger-ui.html.

---

## Exemplos de chamadas

Os exemplos usam `curl` no bash (Linux, macOS ou Git Bash no Windows).

**Cadastrar uma tarefa:**

```bash
curl -i -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Estudar Spring Boot","description":"Revisar o capítulo de JPA","priority":"HIGH","dueDate":"2026-12-31"}'
```

```json
{
  "status": "CREATED",
  "code": 201,
  "message": "Tarefa criada com sucesso",
  "payload": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "title": "Estudar Spring Boot",
    "description": "Revisar o capítulo de JPA",
    "priority": "HIGH",
    "status": "PENDENT",
    "dueDate": "2026-12-31",
    "createdAt": "2026-11-01T14:30:00.123Z",
    "updatedAt": "2026-11-01T14:30:00.123Z"
  },
  "timestamp": "2026-11-01T14:30:00.130Z",
  "error": false
}
```

**Listar com filtros combinados, ordenação e paginação:**

```bash
curl "http://localhost:8080/api/tasks?title=estudar&status=PENDENT&priority=HIGH&sort=DUE_DATE&direction=ASC&page=0&size=5"
```

```json
{
  "status": "OK",
  "code": 200,
  "message": "Tarefas listadas com sucesso",
  "payload": {
    "items": [ { "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6", "title": "Estudar Spring Boot", "...": "..." } ],
    "page": 0,
    "size": 5,
    "totalItems": 1,
    "totalPages": 1
  },
  "timestamp": "2026-11-01T14:31:00.000Z",
  "error": false
}
```

**Consultar, editar, concluir, reabrir e excluir** (troque `$ID` pelo UUID retornado no cadastro):

```bash
ID=3fa85f64-5717-4562-b3fc-2c963f66afa6

curl http://localhost:8080/api/tasks/$ID

curl -X PUT http://localhost:8080/api/tasks/$ID \
  -H "Content-Type: application/json" \
  -d '{"title":"Estudar Spring Boot 4","description":null,"priority":"MEDIUM","status":"ON_GOING","dueDate":null}'

curl -X PATCH http://localhost:8080/api/tasks/$ID/complete
curl -X PATCH http://localhost:8080/api/tasks/$ID/reopen
curl -X DELETE http://localhost:8080/api/tasks/$ID
```

**Dashboard:**

```bash
curl http://localhost:8080/api/dashboard
```

```json
{
  "status": "OK",
  "code": 200,
  "message": "Dashboard carregado com sucesso",
  "payload": { "total": 4, "byStatus": { "PENDENT": 2, "ON_GOING": 1, "DONE": 1 } },
  "timestamp": "2026-11-01T14:32:00.000Z",
  "error": false
}
```

**Erros:**

```bash
# Título vazio → 400 com o detalhe por campo
curl -X POST http://localhost:8080/api/tasks -H "Content-Type: application/json" -d '{"title":"","priority":"LOW"}'
# {"status":"BAD_REQUEST","code":400,"message":"Dados inválidos","payload":{"title":"O título é obrigatório"},...,"error":true}

# UUID malformado → 400
curl http://localhost:8080/api/tasks/abc
# {"status":"BAD_REQUEST","code":400,"message":"Identificador inválido: informe um UUID válido","payload":null,...,"error":true}

# UUID válido e inexistente → 404
curl http://localhost:8080/api/tasks/00000000-0000-0000-0000-000000000000
# {"status":"NOT_FOUND","code":404,"message":"Tarefa não encontrada: 00000000-...","payload":null,...,"error":true}
```

---

## Funcionalidades concluídas

### Requisitos obrigatórios
- [x] Cadastro com título obrigatório, descrição opcional, prioridade (`LOW`, `MEDIUM`, `HIGH`), status (`PENDENT`, `ON_GOING`, `DONE`) e vencimento opcional
- [x] Consulta individual pelo UUID
- [x] Edição de título, descrição, prioridade, vencimento e status
- [x] Concluir e reabrir tarefas
- [x] Exclusão, com 404 quando o recurso não existe
- [x] Listagem com busca parcial por título, filtros de status e prioridade, ordenação por título, criação ou vencimento, e paginação no servidor, com filtros combináveis
- [x] Dashboard com o total e os totais por status
- [x] UUID gerado pelo backend (`java.util.UUID` e tipo nativo `UUID` no PostgreSQL 18); 400 para UUID malformado e 404 para inexistente
- [x] Onion Architecture, DTOs, validação de entrada e tratamento centralizado de exceções
- [x] Contrato JSON em todas as respostas com corpo, inclusive nos erros, sem expor detalhes internos
- [x] Frontend com Angular Material (MatTable, MatPaginator, MatSort, MatFormField, MatSelect, MatDialog, MatCard), roteamento e Reactive Forms com validação
- [x] Estados de carregamento, erro, ausência de dados e confirmação; layout responsivo
- [x] Dockerfile do backend, Dockerfile do frontend e `docker-compose.yml` com Angular, Spring Boot e PostgreSQL 18
- [x] Variáveis de ambiente, volume persistente e `.env.example`, sem segredos versionados

### Diferenciais
- [x] Testes unitários com JUnit 5 e Mockito
- [x] Cenários de comportamento executáveis com Cucumber e Gherkin
- [x] Testes Angular (Vitest)
- [x] Documentação da API com OpenAPI e Swagger UI
- [x] Migrations com Flyway
- [x] Dockerfiles multi-stage

### Extras
- Cards do dashboard clicáveis, que abrem a listagem já filtrada (`/tasks?status=DONE`)
- Barra de progresso com o percentual de tarefas concluídas
- Snackbar de sucesso nas operações e diálogo de confirmação antes de excluir
- No celular, a listagem mostra menos colunas e agrupa as ações num menu
- Interface em português: datas, paginador e datepicker (dd/mm/aaaa)

---

## Decisões técnicas

- **`PUT` para edição e `PATCH` para concluir e reabrir.** O `PUT` substitui todos os campos editáveis, por isso o `status` é obrigatório nele. Concluir e reabrir são ações pontuais, com rotas próprias, e não exigem enviar a tarefa inteira.
- **`DELETE` responde 200 com `payload: null`, e não 204.** O contrato do desafio pede corpo JSON nas respostas; um 204 não pode ter corpo, então a confirmação da exclusão vem no `message`.
- **O id do corpo é ignorado no cadastro e na edição.** Os DTOs não têm o campo `id`, e o UUID da edição vem sempre da URL. Assim, o cliente não consegue definir nem alterar o identificador.
- **Os objetos de consulta ficam no domínio** (`domain/query`). Filtros, ordenação e paginação são conceitos do negócio, independentes do Spring Data; o adaptador traduz `TaskQuery` para `Specification` e `Pageable`.
- **A ordenação tem um critério de desempate pelo `id`.** Sem ele, tarefas com o mesmo título ou a mesma data podiam trocar de posição entre páginas.
- **O LIKE da busca escapa `%` e `_`.** Buscar `100%` procura o texto literal, e não "qualquer coisa que comece com 100".
- **`size` é limitado a 100**, para que uma requisição não peça a tabela inteira.
- **`open-in-view: false`.** A sessão do banco não fica aberta durante a serialização da resposta.
- **No Docker, o frontend chama a API pelo proxy do nginx** (`/api`), na mesma origem. Fora do Docker (`ng serve`), chama `http://localhost:8080/api`, e por isso a API tem o CORS configurável.

---

## Limitações conhecidas

- **Não há autenticação nem separação por usuário**: todas as tarefas são visíveis para quem acessa a aplicação. O desafio não pede isso.
- **A busca por título usa `lower(title) LIKE '%termo%'`**, que não aproveita índice B-tree. Para volumes grandes, um índice com a extensão `pg_trgm` resolveria.
- **Não há controle de concorrência otimista** (`@Version`): se duas pessoas editarem a mesma tarefa ao mesmo tempo, vale a última gravação.
- **O 403 de uma requisição CORS de origem não permitida sai em texto puro**, fora do contrato JSON. Ele é gerado pelo filtro de CORS do Spring antes de a requisição chegar ao controller. No uso normal (Docker com proxy, ou `ng serve` na origem permitida), isso não acontece.
- **Tarefas sem vencimento ficam no fim na ordenação crescente por vencimento, e no início na decrescente**, que é o comportamento padrão do PostgreSQL para `NULL`.
- **Os testes de integração e o Cucumber precisam do Docker rodando** (Testcontainers), e por isso não rodam dentro do build das imagens.
- **Não há testes end-to-end** do frontend (Playwright ou Cypress); os testes Angular são de componente e de serviço.
- **A interface existe só em português**, sem internacionalização.

---

## Executando sem Docker (desenvolvimento)

**Pré-requisitos:** Java 25, Node 22.22+ ou 24.15+, e o Docker (só para o banco).

```bash
# 1. Banco de dados
cp .env.example .env
docker compose up -d postgres

# 2. API (lê o .env da raiz automaticamente)
cd backend
./mvnw spring-boot:run          # http://localhost:8080

# 3. Frontend, em outro terminal
cd frontend
npm ci
npx ng serve                    # http://localhost:4200
```

---

## Licença

[MIT](LICENSE)
