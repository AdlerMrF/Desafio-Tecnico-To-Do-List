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

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTaskUseCaseTest {

    private static final Instant CREATED = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private UpdateTaskUseCase useCase;

    @Test
    void deveAtualizarOsCamposEOUpdatedAt() {
        UUID id = UUID.randomUUID();
        Task existing = Task.restore(id, "Título antigo", null, Priority.LOW,
                TaskStatus.PENDENT, null, CREATED, CREATED);
        when(taskRepository.findById(id)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task updated = useCase.execute(id, new UpdateTaskCommand(
                "Título novo", "Agora com descrição", Priority.HIGH,
                TaskStatus.ON_GOING, LocalDate.of(2026, 12, 31)));

        assertThat(updated.getId()).isEqualTo(id);
        assertThat(updated.getTitle()).isEqualTo("Título novo");
        assertThat(updated.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(updated.getStatus()).isEqualTo(TaskStatus.ON_GOING);
        assertThat(updated.getCreatedAt()).isEqualTo(CREATED);
        assertThat(updated.getUpdatedAt()).isAfter(CREATED);
    }

    @Test
    void deveLancarNotFoundSemSalvarQuandoATarefaNaoExiste() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(id, new UpdateTaskCommand(
                "Qualquer", null, Priority.LOW, TaskStatus.PENDENT, null)))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).save(any());
    }
}
