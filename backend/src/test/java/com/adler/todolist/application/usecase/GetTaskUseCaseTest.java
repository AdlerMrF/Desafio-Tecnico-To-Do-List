package com.adler.todolist.application.usecase;

import com.adler.todolist.domain.exception.TaskNotFoundException;
import com.adler.todolist.domain.model.Priority;
import com.adler.todolist.domain.model.Task;
import com.adler.todolist.domain.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTaskUseCaseTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private GetTaskUseCase useCase;

    @Test
    void deveRetornarATarefaQuandoExiste() {
        Task task = Task.create("Revisar PR", null, Priority.MEDIUM, null, null);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        Task found = useCase.execute(task.getId());

        assertThat(found.getTitle()).isEqualTo("Revisar PR");
    }

    @Test
    void deveLancarNotFoundQuandoNaoExiste() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(id))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(id.toString());
    }
}
