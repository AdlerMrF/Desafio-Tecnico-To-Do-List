# Frontend: To-Do List

Interface do To-Do List, em Angular 22 com Angular Material. Para rodar o projeto inteiro (banco, API e interface), siga o [README da raiz](../README.md).

## Como rodar

**Com Docker:** na raiz do projeto, rode `docker compose up --build` e acesse http://localhost:4200.

**Para desenvolver**, com recarga automática e o backend rodando na porta 8080:

```bash
npm ci
npm start          # ng serve em http://localhost:4200
```

No desenvolvimento, a aplicação chama a API direto em `http://localhost:8080/api` (`environment.development.ts`), liberada pelo CORS do backend. No build de produção, ela chama `/api` (`environment.ts`), e o nginx repassa essas chamadas ao backend.

## Scripts

| Comando | O que faz |
|---|---|
| `npm start` | Servidor de desenvolvimento em http://localhost:4200 |
| `npx ng build` | Build de produção em `dist/frontend/browser` |
| `npx ng test --watch=false` | Roda os testes uma vez (Vitest) |
| `npx ng test --watch=false --coverage` | Roda os testes e gera o relatório em `coverage/index.html` |
| `npx ng test` | Roda os testes em modo watch |

## Telas

| Rota | Tela |
|---|---|
| `/` | Dashboard: total e cards por status (que abrem a listagem filtrada), mais a barra de progresso |
| `/tasks` | Listagem com busca, filtros de status e prioridade, ordenação e paginação no servidor. Aceita `?status=DONE` |
| `/tasks/new` | Formulário de criação |
| `/tasks/:id` | Detalhe, com as ações de editar, concluir ou reabrir e excluir |
| `/tasks/:id/edit` | Formulário de edição |

Qualquer outra rota volta para o dashboard. Cada tela é carregada sob demanda (`loadComponent`).

## Estrutura

```
src/app/
├── core/
│   ├── i18n/            adaptador de datas dd/mm/aaaa e paginador em português
│   ├── interceptors/    api-error-interceptor: converte erros HTTP em ApiError
│   ├── models/          Task, Page, ApiResponse, Dashboard, ApiError...
│   └── services/        TaskService (API), ConfirmService (diálogo), NotificationService (snackbar)
├── features/
│   ├── dashboard/       dashboard-page
│   └── tasks/           task-list, task-detail, task-form
├── shared/
│   └── confirm-dialog/  diálogo de confirmação reutilizável
├── app.config.ts        providers: rotas, HttpClient + interceptor, locale pt-BR
└── app.routes.ts        rotas com loadComponent
```

## Decisões

- **Componentes standalone com Signals.** O estado de cada tela (dados, carregando, erro) fica em `signal` e `computed`. As buscas usam `toObservable` com `switchMap`, que cancela a requisição anterior quando o filtro muda.
- **Filtros com debounce de 400 ms**, para não fazer uma chamada à API por tecla digitada.
- **Parâmetros de rota como inputs** (`withComponentInputBinding`). O `id` do detalhe e o `?status=` da listagem chegam como `input()`. Um status inválido na URL é ignorado, em vez de gerar um erro 400.
- **Tratamento de erros centralizado.** O `apiErrorInterceptor` converte qualquer falha em `ApiError`, com a mensagem do contrato da API. Para servidor fora do ar, a mensagem é "Não foi possível conectar ao servidor". Para corpos fora do contrato, como um HTML de proxy, a mensagem é genérica. Os erros de validação aparecem no campo correspondente do formulário.
- **Feedback.** Os erros ficam numa faixa vermelha até o usuário ler. Os sucessos aparecem num snackbar de 3 segundos. Excluir sempre passa pelo diálogo de confirmação.
- **Português de ponta a ponta.** O `LOCALE_ID` é `pt-BR`, o datepicker aceita `dd/mm/aaaa` e rejeita datas impossíveis (como 31/02), e o paginador é traduzido.
- **Responsivo.** O `BreakpointObserver` detecta telas de até 600px. Nelas, a listagem mostra 3 colunas e as ações viram um menu "⋮".

## Testes: 42 testes em 11 arquivos

| Arquivo | Testes |
|---|---|
| `app.spec.ts` | 2 |
| `task-service.spec.ts` | 6 |
| `api-error-interceptor.spec.ts` | 5 |
| `pt-br-date-adapter.spec.ts` | 5 |
| `paginator-intl.spec.ts` | 4 |
| `notification-service.spec.ts` | 1 |
| `confirm-dialog.spec.ts` | 2 |
| `task-list.spec.ts` | 7 |
| `task-detail.spec.ts` | 2 |
| `task-form.spec.ts` | 4 |
| `dashboard-page.spec.ts` | 4 |

As chamadas HTTP são simuladas com `HttpTestingController`, então os testes não precisam do backend rodando.
