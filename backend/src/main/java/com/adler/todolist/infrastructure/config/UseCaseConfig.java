package com.adler.todolist.infrastructure.config;

import com.adler.todolist.application.usecase.ChangeTaskStatusUseCase;
import com.adler.todolist.application.usecase.CreateTaskUseCase;
import com.adler.todolist.application.usecase.DeleteTaskUseCase;
import com.adler.todolist.application.usecase.GetDashboardUseCase;
import com.adler.todolist.application.usecase.GetTaskUseCase;
import com.adler.todolist.application.usecase.ListTasksUseCase;
import com.adler.todolist.application.usecase.UpdateTaskUseCase;
import com.adler.todolist.domain.repository.TaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra os casos de uso no Spring. A camada de aplicação é Java puro (sem @Service),
 * então quem a conecta ao framework é a infraestrutura.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public CreateTaskUseCase createTaskUseCase(TaskRepository taskRepository) {
        return new CreateTaskUseCase(taskRepository);
    }

    @Bean
    public GetTaskUseCase getTaskUseCase(TaskRepository taskRepository) {
        return new GetTaskUseCase(taskRepository);
    }

    @Bean
    public UpdateTaskUseCase updateTaskUseCase(TaskRepository taskRepository) {
        return new UpdateTaskUseCase(taskRepository);
    }

    @Bean
    public ChangeTaskStatusUseCase changeTaskStatusUseCase(TaskRepository taskRepository) {
        return new ChangeTaskStatusUseCase(taskRepository);
    }

    @Bean
    public DeleteTaskUseCase deleteTaskUseCase(TaskRepository taskRepository) {
        return new DeleteTaskUseCase(taskRepository);
    }

    @Bean
    public ListTasksUseCase listTasksUseCase(TaskRepository taskRepository) {
        return new ListTasksUseCase(taskRepository);
    }

    @Bean
    public GetDashboardUseCase getDashboardUseCase(TaskRepository taskRepository) {
        return new GetDashboardUseCase(taskRepository);
    }
}
