package com.adler.todolist.web.dto;

import com.adler.todolist.domain.model.Priority;
import com.adler.todolist.domain.model.TaskStatus;
import com.adler.todolist.domain.query.SortDirection;
import com.adler.todolist.domain.query.TaskQuery;
import com.adler.todolist.domain.query.TaskSortField;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ListTasksRequestTest {

    @Test
    void deveAplicarOsPadroesQuandoNadaEInformado() {
        TaskQuery query = new ListTasksRequest(null, null, null, null, null, null, null).toQuery();

        assertThat(query.filter().title()).isNull();
        assertThat(query.sortField()).isEqualTo(TaskSortField.CREATED_AT);
        assertThat(query.direction()).isEqualTo(SortDirection.DESC);
        assertThat(query.page()).isZero();
        assertThat(query.size()).isEqualTo(10);
    }

    @Test
    void deveIgnorarTituloEmBrancoERemoverEspacos() {
        assertThat(new ListTasksRequest("   ", null, null, null, null, null, null)
                .toQuery().filter().title()).isNull();
        assertThat(new ListTasksRequest("  estudar ", null, null, null, null, null, null)
                .toQuery().filter().title()).isEqualTo("estudar");
    }

    @Test
    void deveRepassarOsFiltrosEAOrdenacaoInformados() {
        TaskQuery query = new ListTasksRequest("spring", TaskStatus.DONE, Priority.HIGH,
                TaskSortField.TITLE, SortDirection.ASC, 2, 25).toQuery();

        assertThat(query.filter().status()).isEqualTo(TaskStatus.DONE);
        assertThat(query.filter().priority()).isEqualTo(Priority.HIGH);
        assertThat(query.sortField()).isEqualTo(TaskSortField.TITLE);
        assertThat(query.direction()).isEqualTo(SortDirection.ASC);
        assertThat(query.page()).isEqualTo(2);
        assertThat(query.size()).isEqualTo(25);
    }
}
