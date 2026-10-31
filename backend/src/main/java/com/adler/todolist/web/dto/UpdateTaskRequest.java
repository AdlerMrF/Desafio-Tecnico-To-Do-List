package com.adler.todolist.web.dto;

import com.adler.todolist.application.usecase.UpdateTaskCommand;
import com.adler.todolist.domain.model.Priority;
import com.adler.todolist.domain.model.TaskStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateTaskRequest(
        @Schema(description = "Título da tarefa", example = "Estudar Spring Boot", maxLength = 150)
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
        String title,

        @Schema(description = "Descrição opcional", example = "Revisar o capítulo de JPA")
        String description,

        @Schema(description = "Prioridade da tarefa", example = "HIGH")
        @NotNull(message = "A prioridade é obrigatória")
        Priority priority,

        @Schema(description = "Status da tarefa, obrigatório", example = "ON_GOING")
        @NotNull(message = "O status é obrigatório")
        TaskStatus status,

        @Schema(description = "Data de vencimento opcional", example = "2026-12-31")
        LocalDate dueDate) {

    public UpdateTaskCommand toCommand() {
        return new UpdateTaskCommand(title, description, priority, status, dueDate);
    }
}
