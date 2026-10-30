package com.adler.todolist.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataTaskRepository extends JpaRepository<TaskEntity, UUID> {
}