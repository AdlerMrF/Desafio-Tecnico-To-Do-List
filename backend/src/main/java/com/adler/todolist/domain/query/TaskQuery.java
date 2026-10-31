package com.adler.todolist.domain.query;

public record TaskQuery(
        TaskFilter filter,
        TaskSortField sortField,
        SortDirection direction,
        int page,
        int size) {
}
