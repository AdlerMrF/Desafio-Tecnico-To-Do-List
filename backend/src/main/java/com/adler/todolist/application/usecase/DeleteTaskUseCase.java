package com.adler.todolist.application.usecase;

import com.adler.todolist.domain.exception.TaskNotFoundException;
import com.adler.todolist.domain.repository.TaskRepository;

import java.util.UUID;

public class DeleteTaskUseCase {

    private final TaskRepository taskRepository;

    public DeleteTaskUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void execute(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }
}
