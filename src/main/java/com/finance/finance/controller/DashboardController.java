package com.finance.finance.controller;

import com.finance.finance.entity.CashFlow;
import com.finance.finance.entity.User;
import com.finance.finance.service.CashFlowService;
import com.finance.finance.service.CategoryService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/dashboard")
public class DashboardController {

    @Autowired
    private CashFlowService cashFlowService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MeterRegistry meterRegistry;

    private final Counter dashboardAccessCounter;

    public DashboardController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.dashboardAccessCounter = Counter.builder("finance.dashboard.access")
                .description("Total number of dashboard accesses")
                .register(meterRegistry);
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        
        dashboardAccessCounter.increment();
        
        User user = (User) authentication.getPrincipal();
        
        // Default to current month if no dates provided
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        BigDecimal totalIncome = cashFlowService.getTotalIncome(user, startDate, endDate);
        BigDecimal totalExpenses = cashFlowService.getTotalExpenses(user, startDate, endDate);
        BigDecimal balance = cashFlowService.getBalance(user, startDate, endDate);
        
        long incomeCount = cashFlowService.getTransactionCount(user, CashFlow.CashFlowType.INCOME, startDate, endDate);
        long expenseCount = cashFlowService.getTransactionCount(user, CashFlow.CashFlowType.EXPENSE, startDate, endDate);
        
        Map<String, Object> overview = new HashMap<>();
        overview.put("period", Map.of(
            "startDate", startDate,
            "endDate", endDate,
            "days", ChronoUnit.DAYS.between(startDate, endDate) + 1
        ));
        overview.put("totals", Map.of(
            "income", totalIncome != null ? totalIncome : BigDecimal.ZERO,
            "expenses", totalExpenses != null ? totalExpenses : BigDecimal.ZERO,
            "balance", balance
        ));
        overview.put("counts", Map.of(
            "incomeTransactions", incomeCount,
            "expenseTransactions", expenseCount,
            "totalTransactions", incomeCount + expenseCount
        ));
        overview.put("averages", Map.of(
            "dailyIncome", calculateDailyAverage(totalIncome, startDate, endDate),
            "dailyExpenses", calculateDailyAverage(totalExpenses, startDate, endDate),
            "dailyBalance", calculateDailyAverage(balance, startDate, endDate)
        ));
        
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<Map<String, Object>> getMonthlySummary(
            @RequestParam(required = false) Integer year,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        
        BigDecimal totalIncome = cashFlowService.getTotalIncome(user, startDate, endDate);
        BigDecimal totalExpenses = cashFlowService.getTotalExpenses(user, startDate, endDate);
        BigDecimal balance = cashFlowService.getBalance(user, startDate, endDate);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("year", year);
        summary.put("totalIncome", totalIncome != null ? totalIncome : BigDecimal.ZERO);
        summary.put("totalExpenses", totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
        summary.put("balance", balance);
        summary.put("savingsRate", calculateSavingsRate(totalIncome, totalExpenses));
        
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/quick-stats")
    public ResponseEntity<Map<String, Object>> getQuickStats(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate weekStart = today.minusDays(7);
        
        // This month
        BigDecimal monthIncome = cashFlowService.getTotalIncome(user, monthStart, today);
        BigDecimal monthExpenses = cashFlowService.getTotalExpenses(user, monthStart, today);
        
        // Last 7 days
        BigDecimal weekIncome = cashFlowService.getTotalIncome(user, weekStart, today);
        BigDecimal weekExpenses = cashFlowService.getTotalExpenses(user, weekStart, today);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("thisMonth", Map.of(
            "income", monthIncome != null ? monthIncome : BigDecimal.ZERO,
            "expenses", monthExpenses != null ? monthExpenses : BigDecimal.ZERO,
            "balance", (monthIncome != null ? monthIncome : BigDecimal.ZERO)
                .subtract(monthExpenses != null ? monthExpenses : BigDecimal.ZERO)
        ));
        stats.put("last7Days", Map.of(
            "income", weekIncome != null ? weekIncome : BigDecimal.ZERO,
            "expenses", weekExpenses != null ? weekExpenses : BigDecimal.ZERO,
            "balance", (weekIncome != null ? weekIncome : BigDecimal.ZERO)
                .subtract(weekExpenses != null ? weekExpenses : BigDecimal.ZERO)
        ));
        
        return ResponseEntity.ok(stats);
    }

    private BigDecimal calculateDailyAverage(BigDecimal amount, LocalDate startDate, LocalDate endDate) {
        if (amount == null || amount.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days == 0) return BigDecimal.ZERO;
        
        return amount.divide(BigDecimal.valueOf(days), 2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calculateSavingsRate(BigDecimal income, BigDecimal expenses) {
        if (income == null || income.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal netIncome = income.subtract(expenses != null ? expenses : BigDecimal.ZERO);
        return netIncome.multiply(BigDecimal.valueOf(100)).divide(income, 2, BigDecimal.ROUND_HALF_UP);
    }
}
