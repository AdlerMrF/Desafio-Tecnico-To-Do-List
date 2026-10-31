package com.adler.todolist.infrastructure.persistence;

import com.adler.todolist.domain.query.TaskFilter;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class TaskSpecifications {

    private TaskSpecifications() {
    }

    static Specification<TaskEntity> matching(TaskFilter filter) {
        List<Specification<TaskEntity>> conditions = new ArrayList<>();

        if (filter.title() != null) {
            conditions.add(titleContains(filter.title()));
        }
        if (filter.status() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.status()));
        }
        if (filter.priority() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("priority"), filter.priority()));
        }

        return Specification.allOf(conditions);
    }

    private static Specification<TaskEntity> titleContains(String term) {
        String pattern = "%" + escapeLike(term.toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.<String>get("title")), pattern, '\\');
    }

    /** Trata % e _ digitados pelo usuário como texto comum, não como curingas do LIKE. */
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
