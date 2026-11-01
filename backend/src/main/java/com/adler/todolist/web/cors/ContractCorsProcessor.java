package com.adler.todolist.web.cors;

import com.adler.todolist.web.response.ApiResponseFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.cors.DefaultCorsProcessor;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Igual ao processador padrão do Spring, mas a requisição recusada pelo CORS (403)
 * responde no contrato JSON, e não com o texto puro "Invalid CORS request".
 */
public class ContractCorsProcessor extends DefaultCorsProcessor {

    private final ObjectMapper objectMapper;

    public ContractCorsProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void rejectRequest(ServerHttpResponse response) throws IOException {
        HttpStatus status = HttpStatus.FORBIDDEN;
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getBody().write(objectMapper.writeValueAsBytes(
                ApiResponseFactory.body(status, "Origem não permitida pelo CORS", null, true)));
        response.flush();
    }
}
