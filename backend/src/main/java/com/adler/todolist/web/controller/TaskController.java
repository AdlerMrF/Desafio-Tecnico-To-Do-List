package com.adler.todolist.web.controller;

import com.adler.todolist.application.usecase.CreateTaskUseCase;
import com.adler.todolist.application.usecase.GetTaskUseCase;
import com.adler.todolist.domain.model.Task;
import com.adler.todolist.web.dto.CreateTaskRequest;
import com.adler.todolist.web.dto.TaskResponse;
import com.adler.todolist.web.response.ApiResponse;
import com.adler.todolist.web.response.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final GetTaskUseCase getTaskUseCase;

    public TaskController(CreateTaskUseCase createTaskUseCase, GetTaskUseCase getTaskUseCase) {
        this.createTaskUseCase = createTaskUseCase;
        this.getTaskUseCase = getTaskUseCase;
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
}
