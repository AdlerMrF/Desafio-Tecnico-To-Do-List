package com.adler.todolist.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI todoListOpenApi() {
        return new OpenAPI().info(new Info()
                .title("To-Do List API")
                .version("1.0.0")
                .description("""
                        API REST para gerenciamento de tarefas, desenvolvida para o desafio técnico \
                        de estágio Full Stack da Place Tecnologia e Inovação.

                        Todas as respostas com corpo seguem o mesmo contrato JSON: \
                        `status`, `code`, `message`, `payload`, `timestamp` e `error`.

                        Tarefas são identificadas por UUID gerado pelo servidor. \
                        UUID malformado retorna 400; UUID válido e inexistente retorna 404.""")
                .license(new License().name("MIT")));
    }
}
