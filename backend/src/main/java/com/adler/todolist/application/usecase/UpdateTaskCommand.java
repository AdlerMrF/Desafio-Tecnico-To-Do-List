package com.adler.todolist.application.usecase;

import com.adler.todolist.domain.model.Priority;
import com.adler.todolist.domain.model.TaskStatus;

import java.time.LocalDate;

public record UpdateTaskCommand(
        String title,
        String description,
        Priority priority,
        TaskStatus status,
        LocalDate dueDate) {
}
