package com.adler.todolist.application.usecase;

import com.adler.todolist.domain.exception.TaskNotFoundException;
import com.adler.todolist.domain.model.Priority;
import com.adler.todolist.domain.model.Task;
import com.adler.todolist.domain.model.TaskStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeTaskStatusUseCaseTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ChangeTaskStatusUseCase useCase;

    @Test
    void deveConcluirATarefa() {
        Task task = Task.create("Estudar", null, Priority.MEDIUM, null, null);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = useCase.complete(task.getId());

        assertThat(result.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void deveReabrirATarefa() {
        Task task = Task.create("Estudar", null, Priority.MEDIUM, TaskStatus.DONE, null);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = useCase.reopen(task.getId());

        assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDENT);
    }

    @Test
    void deveLancarNotFoundAoConcluirTarefaInexistente() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.complete(id))
                .isInstanceOf(TaskNotFoundException.class);
    }
}
