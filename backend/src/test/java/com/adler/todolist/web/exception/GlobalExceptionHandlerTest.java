package com.adler.todolist.web.exception;

import com.adler.todolist.domain.exception.InvalidTaskException;
import com.adler.todolist.domain.exception.TaskNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    @RestController
    static class FakeController {

        @GetMapping("/fake/{id}")
        String find(@PathVariable UUID id) {
            throw new TaskNotFoundException(id);
        }

        @GetMapping("/fake-invalid")
        String invalid() {
            throw new InvalidTaskException("O título é obrigatório");
        }

        @GetMapping("/fake-error")
        String unexpected() {
            throw new IllegalStateException("detalhe interno secreto");
        }
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FakeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void uuidMalformadoDeveRetornar400NoContrato() throws Exception {
        mockMvc.perform(get("/fake/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Identificador inválido: informe um UUID válido"))
                .andExpect(jsonPath("$.payload").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.error").value(true));
    }

    @Test
    void uuidValidoInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(get("/fake/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.error").value(true));
    }

    @Test
    void regraDoDominioDeveRetornar400ComAMensagem() throws Exception {
        mockMvc.perform(get("/fake-invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O título é obrigatório"));
    }

    @Test
    void erroInesperadoDeveRetornar500SemDetalhesInternos() throws Exception {
        mockMvc.perform(get("/fake-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Erro interno do servidor"))
                .andExpect(content().string(not(containsString("secreto"))));
    }
}