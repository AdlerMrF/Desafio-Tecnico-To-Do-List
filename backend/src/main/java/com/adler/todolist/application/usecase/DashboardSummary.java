package com.adler.todolist.application.usecase;

import com.adler.todolist.domain.model.TaskStatus;

import java.util.Map;

public record DashboardSummary(long total, Map<TaskStatus, Long> byStatus) {
}
