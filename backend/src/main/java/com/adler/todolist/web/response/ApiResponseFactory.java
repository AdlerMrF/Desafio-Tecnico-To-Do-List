package com.adler.todolist.web.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

public final class ApiResponseFactory {

    private ApiResponseFactory() {
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T payload) {
        return build(HttpStatus.OK, message, payload, false);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T payload) {
        return build(HttpStatus.CREATED, message, payload, false);
    }

    public static <T> ApiResponse<T> body(HttpStatus status, String message, T payload, boolean error) {
        return new ApiResponse<>(status.name(), status.value(), message, payload, Instant.now(), error);
    }

    private static <T> ResponseEntity<ApiResponse<T>> build(HttpStatus status, String message,
                                                            T payload, boolean error) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body(status, message, payload, error));
    }
}
