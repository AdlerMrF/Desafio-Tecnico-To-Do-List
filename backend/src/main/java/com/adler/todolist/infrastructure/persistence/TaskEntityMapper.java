package com.adler.todolist.infrastructure.persistence;

import com.adler.todolist.domain.model.Task;

final class TaskEntityMapper {

    private TaskEntityMapper() {
    }

    static TaskEntity toEntity(Task task) {
        return new TaskEntity(
                task.getId(), task.getTitle(), task.getDescription(), task.getPriority(),
                task.getStatus(), task.getDueDate(), task.getCreatedAt(), task.getUpdatedAt());
    }

    static Task toDomain(TaskEntity entity) {
        return Task.restore(
                entity.getId(), entity.getTitle(), entity.getDescription(), entity.getPriority(),
                entity.getStatus(), entity.getDueDate(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}