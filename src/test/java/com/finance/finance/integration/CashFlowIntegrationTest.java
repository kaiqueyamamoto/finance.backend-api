package com.finance.finance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.finance.dto.CashFlowRequest;
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
public class CashFlowIntegrationTest {

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
    private Category testCategory;

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

        // Create test category
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setDescription("Test Description");
        testCategory.setType(Category.CategoryType.EXPENSE);
        testCategory.setIsActive(true);
        testCategory = categoryRepository.save(testCategory);

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
    void testCreateCashFlow() throws Exception {
        CashFlowRequest request = new CashFlowRequest();
        request.setDescription("Test Expense");
        request.setAmount(new BigDecimal("100.50"));
        request.setTransactionDate(LocalDate.now());
        request.setType(CashFlow.CashFlowType.EXPENSE);
        request.setCategoryId(testCategory.getId());
        request.setNotes("Test notes");

        mockMvc.perform(post("/auth/cashflow")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Test Expense"))
                .andExpect(jsonPath("$.amount").value(100.50))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.category.id").value(testCategory.getId().intValue()))
                .andExpect(jsonPath("$.notes").value("Test notes"));
    }

    @Test
    void testCreateCashFlowWithoutAuth() throws Exception {
        CashFlowRequest request = new CashFlowRequest();
        request.setDescription("Test Expense");
        request.setAmount(new BigDecimal("100.50"));
        request.setTransactionDate(LocalDate.now());
        request.setType(CashFlow.CashFlowType.EXPENSE);
        request.setCategoryId(testCategory.getId());

        mockMvc.perform(post("/auth/cashflow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateCashFlowWithInvalidCategory() throws Exception {
        CashFlowRequest request = new CashFlowRequest();
        request.setDescription("Test Expense");
        request.setAmount(new BigDecimal("100.50"));
        request.setTransactionDate(LocalDate.now());
        request.setType(CashFlow.CashFlowType.EXPENSE);
        request.setCategoryId(999L); // Non-existent category

        mockMvc.perform(post("/auth/cashflow")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCashFlowWithInvalidAmount() throws Exception {
        CashFlowRequest request = new CashFlowRequest();
        request.setDescription("Test Expense");
        request.setAmount(new BigDecimal("-100.50")); // Negative amount
        request.setTransactionDate(LocalDate.now());
        request.setType(CashFlow.CashFlowType.EXPENSE);
        request.setCategoryId(testCategory.getId());

        mockMvc.perform(post("/auth/cashflow")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCashFlows() throws Exception {
        // Create some test cash flows
        createTestCashFlow("Expense 1", new BigDecimal("100.00"), CashFlow.CashFlowType.EXPENSE);
        createTestCashFlow("Income 1", new BigDecimal("500.00"), CashFlow.CashFlowType.INCOME);

        mockMvc.perform(get("/auth/cashflow")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].description").exists())
                .andExpect(jsonPath("$.content[0].amount").exists())
                .andExpect(jsonPath("$.content[0].type").exists());
    }

    @Test
    void testGetCashFlowById() throws Exception {
        // Create a test cash flow
        CashFlow cashFlow = createTestCashFlow("Test Expense", new BigDecimal("100.00"), CashFlow.CashFlowType.EXPENSE);

        mockMvc.perform(get("/auth/cashflow/" + cashFlow.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cashFlow.getId().intValue()))
                .andExpect(jsonPath("$.description").value("Test Expense"))
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    void testGetCashFlowByIdNotFound() throws Exception {
        mockMvc.perform(get("/auth/cashflow/999")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCashFlowsByDateRange() throws Exception {
        // Create cash flows with different dates
        createTestCashFlowWithDate("Expense 1", new BigDecimal("100.00"), LocalDate.now().minusDays(5));
        createTestCashFlowWithDate("Expense 2", new BigDecimal("200.00"), LocalDate.now().minusDays(2));
        createTestCashFlowWithDate("Expense 3", new BigDecimal("300.00"), LocalDate.now().plusDays(1));

        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        mockMvc.perform(get("/auth/cashflow/date-range")
                .header("Authorization", "Bearer " + authToken)
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2))); // Only 2 should be in range
    }

    @Test
    void testGetCashFlowsByType() throws Exception {
        // Create cash flows of different types
        createTestCashFlow("Expense 1", new BigDecimal("100.00"), CashFlow.CashFlowType.EXPENSE);
        createTestCashFlow("Expense 2", new BigDecimal("200.00"), CashFlow.CashFlowType.EXPENSE);
        createTestCashFlow("Income 1", new BigDecimal("500.00"), CashFlow.CashFlowType.INCOME);

        mockMvc.perform(get("/auth/cashflow/type/EXPENSE")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type").value("EXPENSE"))
                .andExpect(jsonPath("$[1].type").value("EXPENSE"));
    }

    @Test
    void testSearchCashFlows() throws Exception {
        // Create cash flows with different descriptions
        createTestCashFlow("Grocery shopping", new BigDecimal("100.00"), CashFlow.CashFlowType.EXPENSE);
        createTestCashFlow("Gas station", new BigDecimal("50.00"), CashFlow.CashFlowType.EXPENSE);
        createTestCashFlow("Salary payment", new BigDecimal("2000.00"), CashFlow.CashFlowType.INCOME);

        mockMvc.perform(get("/auth/cashflow/search")
                .header("Authorization", "Bearer " + authToken)
                .param("term", "shopping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description").value("Grocery shopping"));
    }

    @Test
    void testUpdateCashFlow() throws Exception {
        // Create a test cash flow
        CashFlow cashFlow = createTestCashFlow("Original Description", new BigDecimal("100.00"), CashFlow.CashFlowType.EXPENSE);

        CashFlowRequest updateRequest = new CashFlowRequest();
        updateRequest.setDescription("Updated Description");
        updateRequest.setAmount(new BigDecimal("150.00"));
        updateRequest.setTransactionDate(LocalDate.now());
        updateRequest.setType(CashFlow.CashFlowType.EXPENSE);
        updateRequest.setCategoryId(testCategory.getId());
        updateRequest.setNotes("Updated notes");

        mockMvc.perform(put("/auth/cashflow/" + cashFlow.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.notes").value("Updated notes"));
    }

    @Test
    void testDeleteCashFlow() throws Exception {
        // Create a test cash flow
        CashFlow cashFlow = createTestCashFlow("To be deleted", new BigDecimal("100.00"), CashFlow.CashFlowType.EXPENSE);

        mockMvc.perform(delete("/auth/cashflow/" + cashFlow.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify it's soft deleted
        mockMvc.perform(get("/auth/cashflow/" + cashFlow.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCashFlowSummary() throws Exception {
        // Create test cash flows
        createTestCashFlow("Income 1", new BigDecimal("1000.00"), CashFlow.CashFlowType.INCOME);
        createTestCashFlow("Income 2", new BigDecimal("500.00"), CashFlow.CashFlowType.INCOME);
        createTestCashFlow("Expense 1", new BigDecimal("200.00"), CashFlow.CashFlowType.EXPENSE);
        createTestCashFlow("Expense 2", new BigDecimal("300.00"), CashFlow.CashFlowType.EXPENSE);

        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        mockMvc.perform(get("/auth/cashflow/summary")
                .header("Authorization", "Bearer " + authToken)
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(1500.00))
                .andExpect(jsonPath("$.totalExpenses").value(500.00))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.transactionCount").exists());
    }

    private CashFlow createTestCashFlow(String description, BigDecimal amount, CashFlow.CashFlowType type) {
        return createTestCashFlowWithDate(description, amount, type, LocalDate.now());
    }

    private CashFlow createTestCashFlowWithDate(String description, BigDecimal amount, LocalDate date) {
        return createTestCashFlowWithDate(description, amount, CashFlow.CashFlowType.EXPENSE, date);
    }

    private CashFlow createTestCashFlowWithDate(String description, BigDecimal amount, CashFlow.CashFlowType type, LocalDate date) {
        CashFlow cashFlow = new CashFlow();
        cashFlow.setDescription(description);
        cashFlow.setAmount(amount);
        cashFlow.setTransactionDate(date);
        cashFlow.setType(type);
        cashFlow.setCategory(testCategory);
        cashFlow.setUser(testUser);
        cashFlow.setIsActive(true);
        return cashFlowRepository.save(cashFlow);
    }
}
