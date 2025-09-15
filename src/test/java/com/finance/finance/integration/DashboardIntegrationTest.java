package com.finance.finance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.finance.dto.LoginRequest;
import com.finance.finance.entity.CashFlow;
import com.finance.finance.entity.Category;
import com.finance.finance.entity.User;
import com.finance.finance.repository.CashFlowRepository;
import com.finance.finance.repository.CategoryRepository;
import com.finance.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class DashboardIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CashFlowRepository cashFlowRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String authToken;
    private User testUser;
    private Category incomeCategory;
    private Category expenseCategory;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean up test data
        cashFlowRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRoles("USER");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);

        // Create test categories
        incomeCategory = new Category();
        incomeCategory.setName("Salary");
        incomeCategory.setDescription("Monthly salary");
        incomeCategory.setType(Category.CategoryType.INCOME);
        incomeCategory.setIsActive(true);
        incomeCategory = categoryRepository.save(incomeCategory);

        expenseCategory = new Category();
        expenseCategory.setName("Groceries");
        expenseCategory.setDescription("Food and groceries");
        expenseCategory.setType(Category.CategoryType.EXPENSE);
        expenseCategory.setIsActive(true);
        expenseCategory = categoryRepository.save(expenseCategory);

        // Login to get auth token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        String response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        authToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void testGetDashboardOverview() throws Exception {
        // Create test cash flows
        createTestCashFlow("Salary", new BigDecimal("5000.00"), CashFlow.CashFlowType.INCOME, LocalDate.now().minusDays(10));
        createTestCashFlow("Freelance", new BigDecimal("1000.00"), CashFlow.CashFlowType.INCOME, LocalDate.now().minusDays(5));
        createTestCashFlow("Groceries", new BigDecimal("200.00"), CashFlow.CashFlowType.EXPENSE, LocalDate.now().minusDays(3));
        createTestCashFlow("Rent", new BigDecimal("800.00"), CashFlow.CashFlowType.EXPENSE, LocalDate.now().minusDays(1));

        mockMvc.perform(get("/auth/dashboard/overview")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(6000.00))
                .andExpect(jsonPath("$.totalExpenses").value(1000.00))
                .andExpect(jsonPath("$.balance").value(5000.00))
                .andExpect(jsonPath("$.transactionCount").value(4))
                .andExpect(jsonPath("$.period").exists());
    }

    @Test
    void testGetDashboardOverviewWithDateRange() throws Exception {
        // Create test cash flows with different dates
        createTestCashFlow("Old Income", new BigDecimal("1000.00"), CashFlow.CashFlowType.INCOME, LocalDate.now().minusDays(40));
        createTestCashFlow("Recent Income", new BigDecimal("2000.00"), CashFlow.CashFlowType.INCOME, LocalDate.now().minusDays(10));
        createTestCashFlow("Old Expense", new BigDecimal("500.00"), CashFlow.CashFlowType.EXPENSE, LocalDate.now().minusDays(35));
        createTestCashFlow("Recent Expense", new BigDecimal("300.00"), CashFlow.CashFlowType.EXPENSE, LocalDate.now().minusDays(5));

        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        mockMvc.perform(get("/auth/dashboard/overview")
                .header("Authorization", "Bearer " + authToken)
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(2000.00))
                .andExpect(jsonPath("$.totalExpenses").value(300.00))
                .andExpect(jsonPath("$.balance").value(1700.00))
                .andExpect(jsonPath("$.transactionCount").value(2));
    }

    @Test
    void testGetDashboardOverviewWithoutAuth() throws Exception {
        mockMvc.perform(get("/auth/dashboard/overview"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetMonthlySummary() throws Exception {
        // Create test cash flows for different months
        createTestCashFlow("January Income", new BigDecimal("5000.00"), CashFlow.CashFlowType.INCOME, LocalDate.of(2024, 1, 15));
        createTestCashFlow("January Expense", new BigDecimal("2000.00"), CashFlow.CashFlowType.EXPENSE, LocalDate.of(2024, 1, 20));
        createTestCashFlow("February Income", new BigDecimal("5500.00"), CashFlow.CashFlowType.INCOME, LocalDate.of(2024, 2, 15));
        createTestCashFlow("February Expense", new BigDecimal("1800.00"), CashFlow.CashFlowType.EXPENSE, LocalDate.of(2024, 2, 20));

        mockMvc.perform(get("/auth/dashboard/monthly-summary")
                .header("Authorization", "Bearer " + authToken)
                .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.monthlyData").isArray())
                .andExpect(jsonPath("$.monthlyData", hasSize(2)))
                .andExpect(jsonPath("$.monthlyData[0].month").exists())
                .andExpect(jsonPath("$.monthlyData[0].income").exists())
                .andExpect(jsonPath("$.monthlyData[0].expenses").exists())
                .andExpect(jsonPath("$.monthlyData[0].balance").exists());
    }

    @Test
    void testGetQuickStats() throws Exception {
        // Create test cash flows
        createTestCashFlow("Today Income", new BigDecimal("100.00"), CashFlow.CashFlowType.INCOME, LocalDate.now());
        createTestCashFlow("Today Expense", new BigDecimal("50.00"), CashFlow.CashFlowType.EXPENSE, LocalDate.now());
        createTestCashFlow("This Week Income", new BigDecimal("500.00"), CashFlow.CashFlowType.INCOME, LocalDate.now().minusDays(3));
        createTestCashFlow("This Week Expense", new BigDecimal("200.00"), CashFlow.CashFlowType.EXPENSE, LocalDate.now().minusDays(2));

        mockMvc.perform(get("/auth/dashboard/quick-stats")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayIncome").value(100.00))
                .andExpect(jsonPath("$.todayExpenses").value(50.00))
                .andExpect(jsonPath("$.todayBalance").value(50.00))
                .andExpect(jsonPath("$.thisWeekIncome").value(600.00))
                .andExpect(jsonPath("$.thisWeekExpenses").value(250.00))
                .andExpect(jsonPath("$.thisWeekBalance").value(350.00))
                .andExpect(jsonPath("$.thisMonthIncome").exists())
                .andExpect(jsonPath("$.thisMonthExpenses").exists())
                .andExpect(jsonPath("$.thisMonthBalance").exists());
    }

    @Test
    void testGetDashboardOverviewWithNoData() throws Exception {
        mockMvc.perform(get("/auth/dashboard/overview")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(0.00))
                .andExpect(jsonPath("$.totalExpenses").value(0.00))
                .andExpect(jsonPath("$.balance").value(0.00))
                .andExpect(jsonPath("$.transactionCount").value(0));
    }

    @Test
    void testGetMonthlySummaryWithNoData() throws Exception {
        mockMvc.perform(get("/auth/dashboard/monthly-summary")
                .header("Authorization", "Bearer " + authToken)
                .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.monthlyData").isArray())
                .andExpect(jsonPath("$.monthlyData", hasSize(0)));
    }

    @Test
    void testGetQuickStatsWithNoData() throws Exception {
        mockMvc.perform(get("/auth/dashboard/quick-stats")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayIncome").value(0.00))
                .andExpect(jsonPath("$.todayExpenses").value(0.00))
                .andExpect(jsonPath("$.todayBalance").value(0.00))
                .andExpect(jsonPath("$.thisWeekIncome").value(0.00))
                .andExpect(jsonPath("$.thisWeekExpenses").value(0.00))
                .andExpect(jsonPath("$.thisWeekBalance").value(0.00));
    }

    @Test
    void testGetDashboardOverviewWithLargeAmounts() throws Exception {
        // Test with large amounts to ensure BigDecimal handling works correctly
        createTestCashFlow("Large Income", new BigDecimal("999999.99"), CashFlow.CashFlowType.INCOME, LocalDate.now());
        createTestCashFlow("Large Expense", new BigDecimal("500000.50"), CashFlow.CashFlowType.EXPENSE, LocalDate.now());

        mockMvc.perform(get("/auth/dashboard/overview")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(999999.99))
                .andExpect(jsonPath("$.totalExpenses").value(500000.50))
                .andExpect(jsonPath("$.balance").value(499999.49))
                .andExpect(jsonPath("$.transactionCount").value(2));
    }

    @Test
    void testGetDashboardOverviewWithRecurringTransactions() throws Exception {
        // Create recurring transactions
        createRecurringCashFlow("Recurring Income", new BigDecimal("1000.00"), CashFlow.CashFlowType.INCOME, "MONTHLY");
        createRecurringCashFlow("Recurring Expense", new BigDecimal("200.00"), CashFlow.CashFlowType.EXPENSE, "WEEKLY");

        mockMvc.perform(get("/auth/dashboard/overview")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(1000.00))
                .andExpect(jsonPath("$.totalExpenses").value(200.00))
                .andExpect(jsonPath("$.balance").value(800.00))
                .andExpect(jsonPath("$.transactionCount").value(2));
    }

    private CashFlow createTestCashFlow(String description, BigDecimal amount, CashFlow.CashFlowType type, LocalDate date) {
        CashFlow cashFlow = new CashFlow();
        cashFlow.setDescription(description);
        cashFlow.setAmount(amount);
        cashFlow.setTransactionDate(date);
        cashFlow.setType(type);
        cashFlow.setCategory(type == CashFlow.CashFlowType.INCOME ? incomeCategory : expenseCategory);
        cashFlow.setUser(testUser);
        cashFlow.setIsActive(true);
        return cashFlowRepository.save(cashFlow);
    }

    private CashFlow createRecurringCashFlow(String description, BigDecimal amount, CashFlow.CashFlowType type, String frequency) {
        CashFlow cashFlow = new CashFlow();
        cashFlow.setDescription(description);
        cashFlow.setAmount(amount);
        cashFlow.setTransactionDate(LocalDate.now());
        cashFlow.setType(type);
        cashFlow.setCategory(type == CashFlow.CashFlowType.INCOME ? incomeCategory : expenseCategory);
        cashFlow.setUser(testUser);
        cashFlow.setIsActive(true);
        cashFlow.setIsRecurring(true);
        cashFlow.setRecurringFrequency(frequency);
        return cashFlowRepository.save(cashFlow);
    }
}
