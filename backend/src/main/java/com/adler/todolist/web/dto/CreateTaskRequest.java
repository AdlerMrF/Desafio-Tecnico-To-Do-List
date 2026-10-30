package com.adler.todolist.web.dto;

import com.adler.todolist.application.usecase.CreateTaskCommand;
import com.adler.todolist.domain.model.Priority;
import com.adler.todolist.domain.model.TaskStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateTaskRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
        String title,

        String description,

        @NotNull(message = "A prioridade é obrigatória")
        Priority priority,

        TaskStatus status,

        LocalDate dueDate) {

    public CreateTaskCommand toCommand() {
        return new CreateTaskCommand(title, description, priority, status, dueDate);
    }
}
