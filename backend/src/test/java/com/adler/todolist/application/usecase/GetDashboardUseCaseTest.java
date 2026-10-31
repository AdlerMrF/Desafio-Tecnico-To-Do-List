package com.adler.todolist.application.usecase;

import com.adler.todolist.domain.model.TaskStatus;
import com.adler.todolist.domain.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDashboardUseCaseTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private GetDashboardUseCase useCase;

    @Test
    void deveSomarOTotalECompletarStatusSemTarefasComZero() {
        when(taskRepository.countByStatus()).thenReturn(Map.of(
                TaskStatus.PENDENT, 3L,
                TaskStatus.DONE, 1L));

        DashboardSummary summary = useCase.execute();

        assertThat(summary.total()).isEqualTo(4);
        assertThat(summary.byStatus())
                .containsEntry(TaskStatus.PENDENT, 3L)
                .containsEntry(TaskStatus.ON_GOING, 0L)
                .containsEntry(TaskStatus.DONE, 1L);
    }

    @Test
    void deveRetornarTudoZeradoQuandoNaoHaTarefas() {
        when(taskRepository.countByStatus()).thenReturn(Map.of());

        DashboardSummary summary = useCase.execute();

        assertThat(summary.total()).isZero();
        assertThat(summary.byStatus()).hasSize(3).containsValues(0L);
    }
}
