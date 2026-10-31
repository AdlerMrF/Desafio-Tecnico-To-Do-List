package com.adler.todolist.application.usecase;

import com.adler.todolist.domain.exception.TaskNotFoundException;
import com.adler.todolist.domain.model.Task;
import com.adler.todolist.domain.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateTaskUseCase {

    private final TaskRepository taskRepository;

    public UpdateTaskUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public Task execute(UUID id, UpdateTaskCommand command) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.update(
                command.title(),
                command.description(),
                command.priority(),
                command.status(),
                command.dueDate());

        return taskRepository.save(task);
    }
}
