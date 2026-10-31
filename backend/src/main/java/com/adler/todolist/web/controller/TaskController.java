package com.adler.todolist.web.controller;

import com.adler.todolist.application.usecase.CreateTaskUseCase;
import com.adler.todolist.domain.model.Task;
import com.adler.todolist.web.dto.CreateTaskRequest;
import com.adler.todolist.web.dto.TaskResponse;
import com.adler.todolist.web.response.ApiResponse;
import com.adler.todolist.web.response.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;

    public TaskController(CreateTaskUseCase createTaskUseCase) {
        this.createTaskUseCase = createTaskUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> create(@Valid @RequestBody CreateTaskRequest request) {
        Task task = createTaskUseCase.execute(request.toCommand());
        return ApiResponseFactory.created("Tarefa criada com sucesso", TaskResponse.from(task));
    }
}
