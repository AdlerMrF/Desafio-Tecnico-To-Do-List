package com.adler.todolist.web.controller.docs;

import com.adler.todolist.web.dto.CreateTaskRequest;
import com.adler.todolist.web.dto.ListTasksRequest;
import com.adler.todolist.web.dto.UpdateTaskRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Tarefas", description = "Cadastro, consulta, edição, conclusão, exclusão e listagem de tarefas")
public interface TaskControllerDocs {

    @Operation(summary = "Listar tarefas",
            description = "Busca parcial por título (sem diferenciar maiúsculas), filtros por status e prioridade "
                    + "combináveis, ordenação e paginação, todos executados no banco. A primeira página é a 0.")
    @ApiResponse(responseCode = "200", description = "Página de tarefas")
    @ApiResponse(responseCode = "400", description = "Parâmetro inválido, como um status inexistente ou size acima de 100",
            content = @Content(schema = @Schema(implementation = com.adler.todolist.web.response.ApiResponse.class)))
    ResponseEntity<?> list(ListTasksRequest request);

    @Operation(summary = "Cadastrar tarefa",
            description = "Cria uma tarefa. O UUID é gerado pelo servidor; um id enviado no corpo é ignorado. "
                    + "Sem status informado, a tarefa começa como PENDENT.")
    @ApiResponse(responseCode = "201", description = "Tarefa criada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos, com os campos e mensagens no payload",
            content = @Content(schema = @Schema(implementation = com.adler.todolist.web.response.ApiResponse.class)))
    ResponseEntity<?> create(CreateTaskRequest request);

    @Operation(summary = "Consultar tarefa por UUID")
    @ApiResponse(responseCode = "200", description = "Tarefa encontrada")
    @ApiResponse(responseCode = "400", description = "UUID malformado",
            content = @Content(schema = @Schema(implementation = com.adler.todolist.web.response.ApiResponse.class)))
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
            content = @Content(schema = @Schema(implementation = com.adler.todolist.web.response.ApiResponse.class)))
    ResponseEntity<?> findById(@Parameter(description = "UUID da tarefa") UUID id);

    @Operation(summary = "Editar tarefa",
            description = "Substitui título, descrição, prioridade, status e vencimento. O id não pode ser alterado.")
    @ApiResponse(responseCode = "200", description = "Tarefa atualizada")
    @ApiResponse(responseCode = "400", description = "UUID malformado ou dados inválidos",
            content = @Content(schema = @Schema(implementation = com.adler.todolist.web.response.ApiResponse.class)))
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
            content = @Content(schema = @Schema(implementation = com.adler.todolist.web.response.ApiResponse.class)))
    ResponseEntity<?> update(@Parameter(description = "UUID da tarefa") UUID id, UpdateTaskRequest request);

    @Operation(summary = "Concluir tarefa", description = "Altera o status para DONE.")
    @ApiResponse(responseCode = "200", description = "Tarefa concluída")
    @ApiResponse(responseCode = "400", description = "UUID malformado",
            content = @Content(schema = @Schema(implementation = com.adler.todolist.web.response.ApiResponse.class)))
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
            content = @Content(schema = @Schema(implementation = com.adler.todolist.web.response.ApiResponse.class)))
    ResponseEntity<?> complete(@Parameter(description = "UUID da tarefa") UUID id);

    @Operation(summary = "Reabrir tarefa", description = "Altera o status para PENDENT.")
    @ApiResponse(responseCode = "200", description = "Tarefa reaberta")
    @ApiResponse(responseCode = "400", description = "UUID malformado",
            content = @Content(schema = @Schema(implementation = com.adler.todolist.web.response.ApiResponse.class)))
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
            content = @Content(schema = @Schema(implementation = com.adler.todolist.web.response.ApiResponse.class)))
    ResponseEntity<?> reopen(@Parameter(description = "UUID da tarefa") UUID id);

    @Operation(summary = "Excluir tarefa", description = "Remove a tarefa. A resposta segue o contrato com payload nulo.")
    @ApiResponse(responseCode = "200", description = "Tarefa excluída")
    @ApiResponse(responseCode = "400", description = "UUID malformado",
            content = @Content(schema = @Schema(implementation = com.adler.todolist.web.response.ApiResponse.class)))
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
            content = @Content(schema = @Schema(implementation = com.adler.todolist.web.response.ApiResponse.class)))
    ResponseEntity<?> delete(@Parameter(description = "UUID da tarefa") UUID id);
}
