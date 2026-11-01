package com.adler.todolist.infrastructure.config;

import com.adler.todolist.TestcontainersConfiguration;
import com.adler.todolist.application.usecase.GetTaskUseCase;
import com.adler.todolist.application.usecase.UpdateTaskCommand;
import com.adler.todolist.application.usecase.UpdateTaskUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.interceptor.TransactionAttribute;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TransactionConfigTest {

    @Autowired
    private UpdateTaskUseCase updateTaskUseCase;

    @Autowired
    private GetTaskUseCase getTaskUseCase;

    @Test
    void osCasosDeUsoDevemSerProxiesTransacionais() {
        assertThat(AopUtils.isAopProxy(updateTaskUseCase)).isTrue();
        assertThat(AopUtils.isAopProxy(getTaskUseCase)).isTrue();
    }

    @Test
    void casosDeUsoDeEscritaUsamTransacaoNormalEOsDeLeituraSomenteLeitura() throws Exception {
        TransactionAttribute update = TransactionConfig.transactionAttribute(
                UpdateTaskUseCase.class.getMethod("execute", UUID.class, UpdateTaskCommand.class),
                UpdateTaskUseCase.class);
        TransactionAttribute get = TransactionConfig.transactionAttribute(
                GetTaskUseCase.class.getMethod("execute", UUID.class), GetTaskUseCase.class);

        assertThat(update).isNotNull();
        assertThat(update.isReadOnly()).isFalse();
        assertThat(get).isNotNull();
        assertThat(get.isReadOnly()).isTrue();
    }

    @Test
    void classesForaDosCasosDeUsoNaoGanhamTransacao() throws Exception {
        assertThat(TransactionConfig.transactionAttribute(
                Object.class.getMethod("toString"), UpdateTaskUseCase.class)).isNull();
        assertThat(TransactionConfig.transactionAttribute(
                String.class.getMethod("length"), String.class)).isNull();
    }
}
