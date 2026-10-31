package com.adler.todolist.web.controller;

import com.adler.todolist.application.usecase.ChangeTaskStatusUseCase;
import com.adler.todolist.application.usecase.CreateTaskUseCase;
import com.adler.todolist.application.usecase.DeleteTaskUseCase;
import com.adler.todolist.application.usecase.GetTaskUseCase;
import com.adler.todolist.application.usecase.UpdateTaskUseCase;
import com.adler.todolist.domain.model.Task;
import com.adler.todolist.web.dto.CreateTaskRequest;
import com.adler.todolist.web.dto.TaskResponse;
import com.adler.todolist.web.dto.UpdateTaskRequest;
import com.adler.todolist.web.response.ApiResponse;
import com.adler.todolist.web.response.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final GetTaskUseCase getTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final ChangeTaskStatusUseCase changeTaskStatusUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;

    public TaskController(CreateTaskUseCase createTaskUseCase,
                          GetTaskUseCase getTaskUseCase,
                          UpdateTaskUseCase updateTaskUseCase,
                          ChangeTaskStatusUseCase changeTaskStatusUseCase,
                          DeleteTaskUseCase deleteTaskUseCase) {
        this.createTaskUseCase = createTaskUseCase;
        this.getTaskUseCase = getTaskUseCase;
        this.updateTaskUseCase = updateTaskUseCase;
        this.changeTaskStatusUseCase = changeTaskStatusUseCase;
        this.deleteTaskUseCase = deleteTaskUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> create(@Valid @RequestBody CreateTaskRequest request) {
        Task task = createTaskUseCase.execute(request.toCommand());
        return ApiResponseFactory.created("Tarefa criada com sucesso", TaskResponse.from(task));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> findById(@PathVariable UUID id) {
        Task task = getTaskUseCase.execute(id);
        return ApiResponseFactory.ok("Tarefa encontrada", TaskResponse.from(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> update(@PathVariable UUID id,
                                                            @Valid @RequestBody UpdateTaskRequest request) {
        Task task = updateTaskUseCase.execute(id, request.toCommand());
        return ApiResponseFactory.ok("Tarefa atualizada com sucesso", TaskResponse.from(task));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> complete(@PathVariable UUID id) {
        Task task = changeTaskStatusUseCase.complete(id);
        return ApiResponseFactory.ok("Tarefa concluída com sucesso", TaskResponse.from(task));
    }

    @PatchMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<TaskResponse>> reopen(@PathVariable UUID id) {
        Task task = changeTaskStatusUseCase.reopen(id);
        return ApiResponseFactory.ok("Tarefa reaberta com sucesso", TaskResponse.from(task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        deleteTaskUseCase.execute(id);
        return ApiResponseFactory.ok("Tarefa excluída com sucesso", null);
    }
}
