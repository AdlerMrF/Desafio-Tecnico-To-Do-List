package com.adler.todolist.infrastructure.persistence;

import com.adler.todolist.domain.model.Task;
import com.adler.todolist.domain.model.TaskStatus;
import com.adler.todolist.domain.query.PageResult;
import com.adler.todolist.domain.query.SortDirection;
import com.adler.todolist.domain.query.TaskQuery;
import com.adler.todolist.domain.query.TaskSortField;
import com.adler.todolist.domain.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TaskRepositoryAdapter implements TaskRepository {

    private final SpringDataTaskRepository jpaRepository;

    public TaskRepositoryAdapter(SpringDataTaskRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Task save(Task task) {
        TaskEntity saved = jpaRepository.save(TaskEntityMapper.toEntity(task));
        return TaskEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return jpaRepository.findById(id).map(TaskEntityMapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Map<TaskStatus, Long> countByStatus() {
        Map<TaskStatus, Long> counts = new EnumMap<>(TaskStatus.class);
        for (Object[] row : jpaRepository.countGroupedByStatus()) {
            counts.put((TaskStatus) row[0], (Long) row[1]);
        }
        return counts;
    }

    @Override
    public PageResult<Task> search(TaskQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.size(),
                toSort(query.sortField(), query.direction()));

        Page<TaskEntity> page = jpaRepository.findAll(TaskSpecifications.matching(query.filter()), pageable);

        return new PageResult<>(
                page.getContent().stream().map(TaskEntityMapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    private static Sort toSort(TaskSortField field, SortDirection direction) {
        String property = switch (field) {
            case TITLE -> "title";
            case CREATED_AT -> "createdAt";
            case DUE_DATE -> "dueDate";
        };
        Sort.Direction springDirection = direction == SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        // O id como desempate garante uma ordem estável entre as páginas.
        return Sort.by(springDirection, property).and(Sort.by("id"));
    }
}