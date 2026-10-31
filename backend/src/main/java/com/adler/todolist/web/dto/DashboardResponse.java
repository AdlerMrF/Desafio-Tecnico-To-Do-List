package com.adler.todolist.web.dto;

import com.adler.todolist.application.usecase.DashboardSummary;
import com.adler.todolist.domain.model.TaskStatus;

import java.util.Map;

public record DashboardResponse(long total, Map<TaskStatus, Long> byStatus) {

    public static DashboardResponse from(DashboardSummary summary) {
        return new DashboardResponse(summary.total(), summary.byStatus());
    }
}
