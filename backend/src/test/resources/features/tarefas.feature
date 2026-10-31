# language: pt
Funcionalidade: Gerenciamento de tarefas

  Cenário: Cadastrar tarefa com sucesso
    Dado que informo uma tarefa válida
    Quando envio a solicitação de cadastro
    Então a API deve retornar HTTP 201
    E a resposta deve conter um UUID
    E a tarefa deve estar persistida