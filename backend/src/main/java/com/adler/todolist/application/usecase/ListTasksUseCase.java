package com.adler.todolist.application.usecase;

import com.adler.todolist.domain.model.Task;
import com.adler.todolist.domain.query.PageResult;
import com.adler.todolist.domain.query.TaskQuery;
import com.adler.todolist.domain.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListTasksUseCase {

    private final TaskRepository taskRepository;

    public ListTasksUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<Task> execute(TaskQuery query) {
        return taskRepository.search(query);
    }
}
