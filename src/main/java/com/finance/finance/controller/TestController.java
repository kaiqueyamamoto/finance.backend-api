package com.finance.finance.controller;

import com.finance.finance.dto.CashFlowRequest;
import com.finance.finance.dto.CashFlowResponse;
import com.finance.finance.entity.CashFlow;
import com.finance.finance.entity.User;
import com.finance.finance.service.CashFlowService;
import com.finance.finance.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/test")
public class TestController {

    @Autowired
    private CashFlowService cashFlowService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/create-user")
    public ResponseEntity<Map<String, String>> createTestUser() {
        // This would normally create a user, but for testing we'll just return success
        Map<String, String> response = new HashMap<>();
        response.put("message", "Test user created (simulated)");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cashflow")
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
