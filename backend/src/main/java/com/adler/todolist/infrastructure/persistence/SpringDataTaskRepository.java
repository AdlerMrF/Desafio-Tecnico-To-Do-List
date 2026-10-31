package com.adler.todolist.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface SpringDataTaskRepository
        extends JpaRepository<TaskEntity, UUID>, JpaSpecificationExecutor<TaskEntity> {
}
