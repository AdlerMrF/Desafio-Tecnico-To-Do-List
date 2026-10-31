package com.adler.todolist.bdd.steps;

import com.adler.todolist.domain.repository.TaskRepository;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TarefaSteps {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TaskRepository taskRepository;

    private String requestBody;
    private MvcResult result;

    @Dado("que informo uma tarefa válida")
    public void queInformoUmaTarefaValida() {
        requestBody = """
                {
                  "title": "Estudar Cucumber",
                  "description": "Escrever o primeiro cenário",
                  "priority": "HIGH",
                  "dueDate": "2026-12-31"
                }
                """;
    }

    @Quando("envio a solicitação de cadastro")
    public void envioASolicitacaoDeCadastro() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        result = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn();
    }

    @Entao("a API deve retornar HTTP {int}")
    public void aApiDeveRetornarHttp(int expectedStatus) {
        assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
    }

    @E("a resposta deve conter um UUID")
    public void aRespostaDeveConterUmUuid() throws Exception {
        String id = JsonPath.read(responseBody(), "$.payload.id");
        assertThat(UUID.fromString(id)).isNotNull();
    }

    @E("a tarefa deve estar persistida")
    public void aTarefaDeveEstarPersistida() throws Exception {
        String id = JsonPath.read(responseBody(), "$.payload.id");
        assertThat(taskRepository.findById(UUID.fromString(id))).isPresent();
    }

    private String responseBody() throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}