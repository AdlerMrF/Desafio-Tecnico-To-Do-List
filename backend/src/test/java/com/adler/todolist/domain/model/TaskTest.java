package com.adler.todolist.domain.model;

import com.adler.todolist.domain.exception.InvalidTaskException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskTest {

    @Test
    void deveCriarTarefaComUuidEStatusPendentePorPadrao() {
        Task task = Task.create("Estudar Angular", null, Priority.HIGH, null, null);

        assertThat(task.getId()).isNotNull();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDENT);
        assertThat(task.getCreatedAt()).isNotNull();
    }

    @Test
    void deveRemoverEspacosDoTitulo() {
        Task task = Task.create("  Estudar Angular  ", null, Priority.LOW, null, null);

        assertThat(task.getTitle()).isEqualTo("Estudar Angular");
    }

    @Test
    void naoDeveCriarTarefaSemTitulo() {
        assertThatThrownBy(() -> Task.create("   ", null, Priority.LOW, null, null))
                .isInstanceOf(InvalidTaskException.class)
                .hasMessage("O título é obrigatório");
    }

    @Test
    void naoDeveCriarTarefaSemPrioridade() {
        assertThatThrownBy(() -> Task.create("Estudar", null, null, null, null))
                .isInstanceOf(InvalidTaskException.class);
    }

    @Test
    void deveConcluirEReabrirTarefa() {
        Task task = Task.create("Estudar", null, Priority.MEDIUM, null, null);

        task.complete();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);

        task.reopen();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDENT);
    }
}