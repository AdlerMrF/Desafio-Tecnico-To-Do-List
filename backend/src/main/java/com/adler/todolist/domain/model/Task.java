package com.adler.todolist.domain.model;

import com.adler.todolist.domain.exception.InvalidTaskException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Task {
    private static final int TITLE_MAX_LENGTH = 150;

    private final UUID id;
    private String title;
    private String description;
    private Priority priority;
    private TaskStatus status;
    private LocalDate dueDate;
    private final Instant createdAt;
    private Instant updatedAt;

        private Task(UUID id, String title, String description, Priority priority,
                 TaskStatus status, LocalDate dueDate, Instant createdAt, Instant updatedAt) {
        if (id == null) {
            throw new InvalidTaskException("O id é obrigatório");
        }
        this.id = id;
        this.title = validateTitle(title);
        this.description = description;
        this.priority = requirePriority(priority);
        this.status = requireStatus(status);
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Cria uma tarefa nova: o UUID é gerado aqui, nunca recebido do cliente. */
    public static Task create(String title, String description, Priority priority,
                              TaskStatus status, LocalDate dueDate) {
        Instant now = Instant.now();
        TaskStatus initialStatus = status != null ? status : TaskStatus.PENDENT;
        return new Task(UUID.randomUUID(), title, description, priority,
                initialStatus, dueDate, now, now);
    }
      /** Reconstrói uma tarefa que já existe no banco. */
    public static Task restore(UUID id, String title, String description, Priority priority,
                               TaskStatus status, LocalDate dueDate,
                               Instant createdAt, Instant updatedAt) {
        return new Task(id, title, description, priority, status, dueDate, createdAt, updatedAt);
    }

    public void update(String title, String description, Priority priority,
                       TaskStatus status, LocalDate dueDate) {
        this.title = validateTitle(title);
        this.description = description;
        this.priority = requirePriority(priority);
        this.status = requireStatus(status);
        this.dueDate = dueDate;
        touch();
    }

    public void complete() {
        this.status = TaskStatus.DONE;
        touch();
    }

    public void reopen() {
        this.status = TaskStatus.PENDENT;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new InvalidTaskException("O título é obrigatório");
        }
        String trimmed = title.strip();
        if (trimmed.length() > TITLE_MAX_LENGTH) {
            throw new InvalidTaskException("O título deve ter no máximo " + TITLE_MAX_LENGTH + " caracteres");
        }
        return trimmed;
    }

    private static Priority requirePriority(Priority priority) {
        if (priority == null) {
            throw new InvalidTaskException("A prioridade é obrigatória");
        }
        return priority;
    }

    private static TaskStatus requireStatus(TaskStatus status) {
        if (status == null) {
            throw new InvalidTaskException("O status é obrigatório");
        }
        return status;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public TaskStatus getStatus() { return status; }
    public LocalDate getDueDate() { return dueDate; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}