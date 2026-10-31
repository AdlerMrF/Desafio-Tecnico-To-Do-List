package com.adler.todolist.web.dto;

import com.adler.todolist.domain.model.Priority;
import com.adler.todolist.domain.model.TaskStatus;
import com.adler.todolist.domain.query.SortDirection;
import com.adler.todolist.domain.query.TaskFilter;
import com.adler.todolist.domain.query.TaskQuery;
import com.adler.todolist.domain.query.TaskSortField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ListTasksRequest(
        String title,
        TaskStatus status,
        Priority priority,
        TaskSortField sort,
        SortDirection direction,

        @Schema(description = "Número da página, começando em 0", example = "0")
        @Min(value = 0, message = "A página deve ser maior ou igual a 0")
        Integer page,

        @Schema(description = "Itens por página, de 1 a 100", example = "10")
        @Min(value = 1, message = "O tamanho da página deve ser pelo menos 1")
        @Max(value = 100, message = "O tamanho da página deve ser no máximo 100")
        Integer size) {

    private static final int DEFAULT_SIZE = 10;

    public TaskQuery toQuery() {
        String trimmedTitle = (title == null || title.isBlank()) ? null : title.strip();
        return new TaskQuery(
                new TaskFilter(trimmedTitle, status, priority),
                sort != null ? sort : TaskSortField.CREATED_AT,
                direction != null ? direction : SortDirection.DESC,
                page != null ? page : 0,
                size != null ? size : DEFAULT_SIZE);
    }
}
