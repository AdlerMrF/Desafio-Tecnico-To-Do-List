package com.adler.todolist.web.response;

import java.time.Instant;

public record ApiResponse<T>(
        String status,
        int code,
        String message,
        T payload,
        Instant timestamp,
        boolean error) {
}
