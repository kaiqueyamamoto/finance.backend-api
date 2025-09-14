package com.finance.finance.controller;

import com.finance.finance.dto.CashFlowRequest;
import com.finance.finance.dto.CashFlowResponse;
import com.finance.finance.entity.User;
import com.finance.finance.service.CashFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/test")
@Tag(name = "Test", description = "Endpoints para testes e desenvolvimento")
public class TestController {

    @Autowired
    private CashFlowService cashFlowService;

    @PostMapping("/create-user")
    @Operation(summary = "Criar usuário de teste", description = "Endpoint para criar um usuário de teste para desenvolvimento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário de teste criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro na criação do usuário")
    })
    public ResponseEntity<Map<String, String>> createTestUser() {
        // This would normally create a user, but for testing we'll just return success
        Map<String, String> response = new HashMap<>();
        response.put("message", "Test user created (simulated)");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cashflow")
    @Operation(summary = "Criar fluxo de caixa de teste", description = "Endpoint para criar um fluxo de caixa de teste")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fluxo de caixa criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro na criação do fluxo de caixa")
    })
    public ResponseEntity<CashFlowResponse> createTestCashFlow(@RequestBody CashFlowRequest request) {
        try {
            // Create a mock user for testing
            User testUser = new User();
            testUser.setId(1L);
            testUser.setUsername("testuser");
            testUser.setEmail("test@example.com");
            testUser.setPassword("password");

            CashFlowResponse response = cashFlowService.createCashFlow(request, testUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Obter dashboard de teste", description = "Endpoint para obter dados do dashboard de teste")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do dashboard obtidos com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao obter dados do dashboard")
    })
    public ResponseEntity<Map<String, Object>> getTestDashboard() {
        try {
            // Create a mock user for testing
            User testUser = new User();
            testUser.setId(1L);
            testUser.setUsername("testuser");
            testUser.setEmail("test@example.com");
            testUser.setPassword("password");

            LocalDate startDate = LocalDate.now().withDayOfMonth(1);
            LocalDate endDate = LocalDate.now();

            BigDecimal totalIncome = cashFlowService.getTotalIncome(testUser, startDate, endDate);
            BigDecimal totalExpenses = cashFlowService.getTotalExpenses(testUser, startDate, endDate);
            BigDecimal balance = cashFlowService.getBalance(testUser, startDate, endDate);

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalIncome", totalIncome != null ? totalIncome : BigDecimal.ZERO);
            dashboard.put("totalExpenses", totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
            dashboard.put("balance", balance);
            dashboard.put("startDate", startDate);
            dashboard.put("endDate", endDate);

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
