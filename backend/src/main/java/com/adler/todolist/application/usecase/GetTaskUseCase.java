package com.adler.todolist.application.usecase;

import com.adler.todolist.domain.exception.TaskNotFoundException;
import com.adler.todolist.domain.model.Task;
import com.adler.todolist.domain.repository.TaskRepository;

import java.util.UUID;

public class GetTaskUseCase {

    private final TaskRepository taskRepository;

    public GetTaskUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task execute(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}
