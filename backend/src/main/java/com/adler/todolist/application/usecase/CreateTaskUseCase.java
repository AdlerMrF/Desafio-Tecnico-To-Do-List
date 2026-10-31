package com.adler.todolist.application.usecase;

import com.adler.todolist.domain.model.Task;
import com.adler.todolist.domain.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateTaskUseCase {

    private final TaskRepository taskRepository;

    public CreateTaskUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
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
