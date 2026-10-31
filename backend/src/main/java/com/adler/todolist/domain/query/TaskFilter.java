package com.adler.todolist.domain.query;

import com.adler.todolist.domain.model.Priority;
import com.adler.todolist.domain.model.TaskStatus;

public record TaskFilter(String title, TaskStatus status, Priority priority) {
}
