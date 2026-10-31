package com.adler.todolist.web.exception;

import com.adler.todolist.domain.exception.InvalidTaskException;
import com.adler.todolist.domain.exception.TaskNotFoundException;
import com.adler.todolist.web.response.ApiResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---- Erros do domínio ----

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Object> handleTaskNotFound(TaskNotFoundException ex) {
        return respond(HttpStatus.NOT_FOUND, ex.getMessage(), null, HttpHeaders.EMPTY);
    }

    @ExceptionHandler(InvalidTaskException.class)
    public ResponseEntity<Object> handleInvalidTask(InvalidTaskException ex) {
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage(), null, HttpHeaders.EMPTY);
    }

    // ---- Qualquer erro não previsto: 500 sem detalhes internos ----

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex) {
        log.error("Erro inesperado ao processar a requisição", ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor", null, HttpHeaders.EMPTY);
    }

    // ---- Erros de entrada tratados pelo Spring ----

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        String message = UUID.class.equals(ex.getRequiredType())
                ? "Identificador inválido: informe um UUID válido"
                : "Parâmetro com formato inválido";
        return respond(HttpStatus.BAD_REQUEST, message, null, headers);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(),
                        error.isBindingFailure() ? "Valor inválido" : error.getDefaultMessage()));
        return respond(HttpStatus.BAD_REQUEST, "Dados inválidos", fields, headers);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        return respond(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido: verifique o JSON e os valores enviados", null, headers);
    }

    /** Qualquer outro erro do Spring MVC (rota inexistente, método não suportado...) também sai no contrato. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return respond(status, defaultMessage(status), null, headers);
    }

    // ---- Montagem única da resposta de erro ----

    private ResponseEntity<Object> respond(HttpStatus status, String message, Object details,
                                           HttpHeaders headers) {
        return ResponseEntity.status(status)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponseFactory.body(status, message, details, true));
    }

    private static String defaultMessage(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "Recurso não encontrado";
            case METHOD_NOT_ALLOWED -> "Método HTTP não suportado para este recurso";
            case UNSUPPORTED_MEDIA_TYPE -> "Tipo de conteúdo não suportado: use application/json";
            default -> status.is4xxClientError() ? "Requisição inválida" : "Erro interno do servidor";
        };
    }
}
