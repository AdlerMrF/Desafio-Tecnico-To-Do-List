package com.adler.todolist.application.usecase;

import com.adler.todolist.domain.model.TaskStatus;
import com.adler.todolist.domain.repository.TaskRepository;

import java.util.EnumMap;
import java.util.Map;

public class GetDashboardUseCase {

    private final TaskRepository taskRepository;

    public GetDashboardUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public DashboardSummary execute() {
        Map<TaskStatus, Long> counts = taskRepository.countByStatus();

        Map<TaskStatus, Long> byStatus = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            byStatus.put(status, counts.getOrDefault(status, 0L));
        }

        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
        return new DashboardSummary(total, byStatus);
    }
}
