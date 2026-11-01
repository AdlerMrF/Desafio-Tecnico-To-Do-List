package com.adler.todolist.application.usecase;

import com.adler.todolist.domain.model.Task;
import com.adler.todolist.domain.repository.TaskRepository;

public class CreateTaskUseCase {

    private final TaskRepository taskRepository;

    public CreateTaskUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task execute(CreateTaskCommand command) {
        Task task = Task.create(
                command.title(),
                command.description(),
                command.priority(),
                command.status(),
                command.dueDate());
        return taskRepository.save(task);
    }
}
