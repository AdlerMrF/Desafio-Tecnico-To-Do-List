package com.adler.todolist.application.usecase;

import com.adler.todolist.domain.exception.InvalidTaskException;
import com.adler.todolist.domain.model.Priority;
import com.adler.todolist.domain.model.Task;
import com.adler.todolist.domain.model.TaskStatus;
import com.adler.todolist.domain.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTaskUseCaseTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private CreateTaskUseCase useCase;

    @Test
    void deveCriarESalvarATarefa() {
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task task = useCase.execute(new CreateTaskCommand(
                "Estudar Spring", "Capítulo 3", Priority.HIGH, null, LocalDate.of(2026, 12, 31)));

        assertThat(task.getId()).isNotNull();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDENT);

        ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(saved.capture());
        assertThat(saved.getValue().getTitle()).isEqualTo("Estudar Spring");
        assertThat(saved.getValue().getDueDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void naoDeveSalvarTarefaInvalida() {
        assertThatThrownBy(() -> useCase.execute(
                new CreateTaskCommand("  ", null, Priority.LOW, null, null)))
                .isInstanceOf(InvalidTaskException.class);

        verifyNoInteractions(taskRepository);
    }
}
