package com.adler.todolist.infrastructure.config;

import com.adler.todolist.application.usecase.CreateTaskUseCase;
import com.adler.todolist.application.usecase.GetDashboardUseCase;
import com.adler.todolist.application.usecase.GetTaskUseCase;
import com.adler.todolist.application.usecase.ListTasksUseCase;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * Abre uma transação em cada método público dos casos de uso, sem que eles usem @Transactional:
 * a camada de aplicação continua sem depender do Spring.
 */
@Configuration
public class TransactionConfig {

    private static final String USE_CASE_PACKAGE = CreateTaskUseCase.class.getPackageName();

    /** Casos de uso que só leem: rodam em transação somente leitura. */
    private static final Set<Class<?>> READ_ONLY_USE_CASES =
            Set.of(GetTaskUseCase.class, ListTasksUseCase.class, GetDashboardUseCase.class);

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static TransactionInterceptor useCaseTransactionInterceptor() {
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionAttributeSource(TransactionConfig::transactionAttribute);
        return interceptor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static TransactionAttributeSourceAdvisor useCaseTransactionAdvisor(
            TransactionInterceptor useCaseTransactionInterceptor) {
        return new TransactionAttributeSourceAdvisor(useCaseTransactionInterceptor);
    }

    /** null para tudo o que não é método público de caso de uso: esses métodos não ganham transação. */
    static TransactionAttribute transactionAttribute(Method method, Class<?> targetClass) {
        Class<?> type = targetClass != null ? targetClass : method.getDeclaringClass();
        if (!USE_CASE_PACKAGE.equals(type.getPackageName())
                || !Modifier.isPublic(method.getModifiers())
                || method.getDeclaringClass() == Object.class) {
            return null;
        }
        RuleBasedTransactionAttribute attribute = new RuleBasedTransactionAttribute();
        attribute.setReadOnly(READ_ONLY_USE_CASES.contains(type));
        return attribute;
    }
}
