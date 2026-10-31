package com.adler.todolist.application.usecase;

import com.adler.todolist.domain.model.Task;
import com.adler.todolist.domain.query.PageResult;
import com.adler.todolist.domain.query.SortDirection;
import com.adler.todolist.domain.query.TaskFilter;
import com.adler.todolist.domain.query.TaskQuery;
import com.adler.todolist.domain.query.TaskSortField;
import com.adler.todolist.domain.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListTasksUseCaseTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ListTasksUseCase useCase;

    @Test
    void deveRepassarABuscaAoRepositorio() {
        TaskQuery query = new TaskQuery(new TaskFilter("estudar", null, null),
                TaskSortField.TITLE, SortDirection.ASC, 0, 10);
        PageResult<Task> page = new PageResult<>(List.of(), 0, 10, 0, 0);
        when(taskRepository.search(query)).thenReturn(page);

        assertThat(useCase.execute(query)).isSameAs(page);
    }
}
