package com.adler.todolist.domain.repository;

import com.adler.todolist.domain.model.Task;

import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(UUID id);
    boolean existsById(UUID id);
    void deleteById(UUID id);
}
