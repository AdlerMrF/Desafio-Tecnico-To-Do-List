# language: pt
Funcionalidade: Gerenciamento de tarefas

  Cenário: Cadastrar tarefa com sucesso
    Dado que informo uma tarefa válida
    Quando envio a solicitação de cadastro
    Então a API deve retornar HTTP 201
    E a resposta deve conter um UUID
    E a tarefa deve estar persistida

  Cenário: Consultar tarefa existente pelo UUID
    Dado que existe uma tarefa cadastrada com o título "Revisar PR"
    Quando consulto essa tarefa pelo UUID
    Então a API deve retornar HTTP 200
    E a resposta deve conter o título "Revisar PR"

  Cenário: Consultar tarefa com UUID malformado
    Quando consulto a tarefa com o identificador "abc-123"
    Então a API deve retornar HTTP 400
    E a resposta deve indicar erro

  Cenário: Consultar tarefa inexistente
    Quando consulto a tarefa com o identificador "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    Então a API deve retornar HTTP 404
    E a resposta deve indicar erro

  Cenário: Editar uma tarefa existente
    Dado que existe uma tarefa cadastrada com o título "Revisar PR"
    Quando edito essa tarefa alterando o título para "Revisar PR do backend" e a prioridade para "HIGH"
    Então a API deve retornar HTTP 200
    E a resposta deve conter o título "Revisar PR do backend"

  Cenário: Concluir e reabrir uma tarefa
    Dado que existe uma tarefa cadastrada com o título "Escrever testes"
    Quando concluo essa tarefa
    Então a API deve retornar HTTP 200
    E a tarefa deve estar com o status "DONE"
    Quando reabro essa tarefa
    Então a tarefa deve estar com o status "PENDENT"

  Cenário: Editar tarefa inexistente
    Quando edito a tarefa com o identificador "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    Então a API deve retornar HTTP 404
    E a resposta deve indicar erro

  Cenário: Excluir uma tarefa existente
    Dado que existe uma tarefa cadastrada com o título "Tarefa descartável"
    Quando excluo essa tarefa
    Então a API deve retornar HTTP 200
    E a tarefa não deve mais existir

  Cenário: Excluir tarefa inexistente
    Quando excluo a tarefa com o identificador "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    Então a API deve retornar HTTP 404
    E a resposta deve indicar erro

  Cenário: Listar tarefas com filtros combinados
    Dado que existem as tarefas:
      | titulo          | prioridade | status  |
      | Estudar Angular | HIGH       | PENDENT |
      | Estudar Spring  | HIGH       | DONE    |
      | Estudar Docker  | LOW        | PENDENT |
      | Revisar PR      | HIGH       | PENDENT |
    Quando listo as tarefas com título "estudar", prioridade "HIGH" e status "PENDENT"
    Então a API deve retornar HTTP 200
    E a listagem deve conter 1 tarefa
    E a primeira tarefa deve ter o título "Estudar Angular"

  Cenário: Ordenar a listagem por título
    Dado que existem as tarefas:
      | titulo  | prioridade | status  |
      | Cuidar  | LOW        | PENDENT |
      | Arrumar | LOW        | PENDENT |
      | Buscar  | LOW        | PENDENT |
    Quando listo as tarefas ordenadas por "TITLE" em ordem "ASC"
    Então a API deve retornar HTTP 200
    E a primeira tarefa deve ter o título "Arrumar"

  Cenário: Paginar a listagem no servidor
    Dado que existem 15 tarefas cadastradas
    Quando listo a página 1 com 10 tarefas por página
    Então a API deve retornar HTTP 200
    E a listagem deve conter 5 tarefas
    E o total de tarefas deve ser 15

  Cenário: Exibir os totais do dashboard
    Dado que existem as tarefas:
      | titulo          | prioridade | status  |
      | Estudar Angular | HIGH       | PENDENT |
      | Estudar Docker  | LOW        | PENDENT |
      | Revisar PR      | MEDIUM     | DONE    |
    Quando consulto o dashboard
    Então a API deve retornar HTTP 200
    E o dashboard deve mostrar 3 tarefas no total
    E o dashboard deve mostrar 2 tarefas com status "PENDENT"
    E o dashboard deve mostrar 0 tarefas com status "ON_GOING"
    E o dashboard deve mostrar 1 tarefa com status "DONE"

  Cenário: Dashboard sem tarefas cadastradas
    Quando consulto o dashboard
    Então a API deve retornar HTTP 200
    E o dashboard deve mostrar 0 tarefas no total