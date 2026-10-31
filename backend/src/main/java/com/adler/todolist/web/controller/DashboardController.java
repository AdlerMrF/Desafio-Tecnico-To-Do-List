package com.adler.todolist.web.controller;

import com.adler.todolist.application.usecase.GetDashboardUseCase;
import com.adler.todolist.web.dto.DashboardResponse;
import com.adler.todolist.web.response.ApiResponse;
import com.adler.todolist.web.response.ApiResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final GetDashboardUseCase getDashboardUseCase;

    public DashboardController(GetDashboardUseCase getDashboardUseCase) {
        this.getDashboardUseCase = getDashboardUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> get() {
        DashboardResponse dashboard = DashboardResponse.from(getDashboardUseCase.execute());
        return ApiResponseFactory.ok("Dashboard carregado com sucesso", dashboard);
    }
}
