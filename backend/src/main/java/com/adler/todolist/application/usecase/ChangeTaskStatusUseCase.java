package com.adler.todolist.application.usecase;

import com.adler.todolist.domain.exception.TaskNotFoundException;
import com.adler.todolist.domain.model.Task;
import com.adler.todolist.domain.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ChangeTaskStatusUseCase {

    private final TaskRepository taskRepository;

    public ChangeTaskStatusUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public Task complete(UUID id) {
        Task task = findOrThrow(id);
        task.complete();
        return taskRepository.save(task);
    }

    @Transactional
    public Task reopen(UUID id) {
        Task task = findOrThrow(id);
        task.reopen();
        return taskRepository.save(task);
    }

    private Task findOrThrow(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}