package com.adler.todolist.web.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Dashboard", description = "Totais de tarefas")
public interface DashboardControllerDocs {

    @Operation(summary = "Totais do dashboard",
            description = "Total de tarefas e totais por status, calculados no banco com uma consulta agrupada. "
                    + "Status sem tarefas aparecem com zero.")
    @ApiResponse(responseCode = "200", description = "Totais calculados")
    ResponseEntity<?> get();
}
