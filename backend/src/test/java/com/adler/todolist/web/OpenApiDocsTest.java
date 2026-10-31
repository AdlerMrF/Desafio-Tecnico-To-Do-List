package com.adler.todolist.web;

import com.adler.todolist.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class OpenApiDocsTest {

    @Autowired
    private WebApplicationContext context;

    @Test
    void deveGerarADocumentacaoComOsCodigosCorretos() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("To-Do List API"))
                .andExpect(jsonPath("$.paths['/api/tasks'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/tasks/{id}'].get.responses['404']").exists());
    }
}
