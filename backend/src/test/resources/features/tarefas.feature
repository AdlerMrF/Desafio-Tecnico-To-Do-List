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