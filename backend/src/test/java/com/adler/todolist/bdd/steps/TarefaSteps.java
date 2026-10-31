package com.adler.todolist.bdd.steps;

import com.adler.todolist.domain.model.Priority;
import com.adler.todolist.domain.model.Task;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class TarefaSteps {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TaskRepository taskRepository;

    private String requestBody;
    private MvcResult result;
    private UUID existingId;

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
        result = mockMvc().perform(post("/api/tasks")
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

    @Dado("que existe uma tarefa cadastrada com o título {string}")
    public void queExisteUmaTarefaCadastradaComOTitulo(String title) {
        Task task = taskRepository.save(Task.create(title, null, Priority.MEDIUM, null, null));
        existingId = task.getId();
    }

    @Quando("consulto essa tarefa pelo UUID")
    public void consultoEssaTarefaPeloUuid() throws Exception {
        result = mockMvc().perform(get("/api/tasks/{id}", existingId)).andReturn();
    }

    @Quando("consulto a tarefa com o identificador {string}")
    public void consultoATarefaComOIdentificador(String id) throws Exception {
        result = mockMvc().perform(get("/api/tasks/{id}", id)).andReturn();
    }

    @E("a resposta deve conter o título {string}")
    public void aRespostaDeveConterOTitulo(String title) throws Exception {
        String actual = JsonPath.read(responseBody(), "$.payload.title");
        assertThat(actual).isEqualTo(title);
    }

    @E("a resposta deve indicar erro")
    public void aRespostaDeveIndicarErro() throws Exception {
        Boolean error = JsonPath.read(responseBody(), "$.error");
        assertThat(error).isTrue();
    }

    @Quando("edito essa tarefa alterando o título para {string} e a prioridade para {string}")
    public void editoEssaTarefa(String title, String priority) throws Exception {
        String body = """
                {"title": "%s", "priority": "%s", "status": "ON_GOING"}
                """.formatted(title, priority);
        result = mockMvc().perform(put("/api/tasks/{id}", existingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
    }

    @Quando("edito a tarefa com o identificador {string}")
    public void editoATarefaComOIdentificador(String id) throws Exception {
        String body = """
                {"title": "Qualquer", "priority": "LOW", "status": "PENDENT"}
                """;
        result = mockMvc().perform(put("/api/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
    }

    @Quando("concluo essa tarefa")
    public void concluoEssaTarefa() throws Exception {
        result = mockMvc().perform(patch("/api/tasks/{id}/complete", existingId)).andReturn();
    }

    @Quando("reabro essa tarefa")
    public void reabroEssaTarefa() throws Exception {
        result = mockMvc().perform(patch("/api/tasks/{id}/reopen", existingId)).andReturn();
    }

    @E("a tarefa deve estar com o status {string}")
    public void aTarefaDeveEstarComOStatus(String status) throws Exception {
        String actual = JsonPath.read(responseBody(), "$.payload.status");
        assertThat(actual).isEqualTo(status);
    }

    @Quando("excluo essa tarefa")
    public void excluoEssaTarefa() throws Exception {
        result = mockMvc().perform(delete("/api/tasks/{id}", existingId)).andReturn();
    }

    @Quando("excluo a tarefa com o identificador {string}")
    public void excluoATarefaComOIdentificador(String id) throws Exception {
        result = mockMvc().perform(delete("/api/tasks/{id}", id)).andReturn();
    }

    @E("a tarefa não deve mais existir")
    public void aTarefaNaoDeveMaisExistir() {
        assertThat(taskRepository.findById(existingId)).isEmpty();
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).build();
    }

    private String responseBody() throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}