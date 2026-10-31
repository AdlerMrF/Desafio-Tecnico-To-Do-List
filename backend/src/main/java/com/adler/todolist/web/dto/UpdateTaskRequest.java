package com.adler.todolist.web.dto;

import com.adler.todolist.application.usecase.UpdateTaskCommand;
import com.adler.todolist.domain.model.Priority;
import com.adler.todolist.domain.model.TaskStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateTaskRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
        String title,

        String description,

        @NotNull(message = "A prioridade é obrigatória")
        Priority priority,

        @NotNull(message = "O status é obrigatório")
        TaskStatus status,

        LocalDate dueDate) {

    public UpdateTaskCommand toCommand() {
        return new UpdateTaskCommand(title, description, priority, status, dueDate);
    }
}