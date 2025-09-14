package com.finance.finance.controller;

import com.finance.finance.dto.CashFlowRequest;
import com.finance.finance.dto.CashFlowResponse;
import com.finance.finance.entity.CashFlow;
import com.finance.finance.entity.User;
import com.finance.finance.service.CashFlowService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth/cashflow")
@Tag(name = "Cash Flow", description = "Endpoints para gerenciamento de fluxo de caixa")
@SecurityRequirement(name = "bearerAuth")
public class CashFlowController {

    @Autowired
    private CashFlowService cashFlowService;

    @Autowired
    private MeterRegistry meterRegistry;

    private final Counter cashFlowCreatedCounter;
    private final Counter cashFlowUpdatedCounter;
    private final Counter cashFlowDeletedCounter;

    public CashFlowController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.cashFlowCreatedCounter = Counter.builder("finance.cashflow.created")
                .description("Total number of cash flow entries created")
                .register(meterRegistry);
        this.cashFlowUpdatedCounter = Counter.builder("finance.cashflow.updated")
                .description("Total number of cash flow entries updated")
                .register(meterRegistry);
        this.cashFlowDeletedCounter = Counter.builder("finance.cashflow.deleted")
                .description("Total number of cash flow entries deleted")
                .register(meterRegistry);
    }

    @PostMapping
    @Operation(summary = "Criar fluxo de caixa", description = "Cria uma nova entrada de fluxo de caixa (receita ou despesa)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fluxo de caixa criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<CashFlowResponse> createCashFlow(
            @Valid @RequestBody CashFlowRequest request,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            CashFlowResponse response = cashFlowService.createCashFlow(request, user);
            cashFlowCreatedCounter.increment();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter fluxo de caixa por ID", description = "Retorna um fluxo de caixa específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fluxo de caixa encontrado"),
            @ApiResponse(responseCode = "404", description = "Fluxo de caixa não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<CashFlowResponse> getCashFlow(
            @Parameter(description = "ID do fluxo de caixa")
            @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Optional<CashFlowResponse> response = cashFlowService.getCashFlowById(id, user);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar fluxos de caixa", description = "Retorna uma lista paginada de fluxos de caixa do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de fluxos de caixa obtida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<Page<CashFlowResponse>> getCashFlows(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Page<CashFlowResponse> response = cashFlowService.getCashFlowsByUser(user, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<CashFlowResponse>> getCashFlowsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<CashFlowResponse> response = cashFlowService.getCashFlowsByDateRange(user, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<CashFlowResponse>> getCashFlowsByType(
            @PathVariable CashFlow.CashFlowType type,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<CashFlowResponse> response = cashFlowService.getCashFlowsByType(user, type);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CashFlowResponse>> searchCashFlows(
            @RequestParam String term,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<CashFlowResponse> response = cashFlowService.searchCashFlows(user, term);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CashFlowResponse> updateCashFlow(
            @PathVariable Long id,
            @Valid @RequestBody CashFlowRequest request,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            CashFlowResponse response = cashFlowService.updateCashFlow(id, request, user);
            cashFlowUpdatedCounter.increment();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCashFlow(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            cashFlowService.deleteCashFlow(id, user);
            cashFlowDeletedCounter.increment();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        BigDecimal totalIncome = cashFlowService.getTotalIncome(user, startDate, endDate);
        BigDecimal totalExpenses = cashFlowService.getTotalExpenses(user, startDate, endDate);
        BigDecimal balance = cashFlowService.getBalance(user, startDate, endDate);
        
        long incomeCount = cashFlowService.getTransactionCount(user, CashFlow.CashFlowType.INCOME, startDate, endDate);
        long expenseCount = cashFlowService.getTransactionCount(user, CashFlow.CashFlowType.EXPENSE, startDate, endDate);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome != null ? totalIncome : BigDecimal.ZERO);
        summary.put("totalExpenses", totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
        summary.put("balance", balance);
        summary.put("incomeCount", incomeCount);
        summary.put("expenseCount", expenseCount);
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);
        
        return ResponseEntity.ok(summary);
    }
}
