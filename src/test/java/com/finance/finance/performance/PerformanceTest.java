package com.finance.finance.performance;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class PerformanceTest {

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
    void testConcurrentLoginRequests() throws Exception {
        int numberOfThreads = 10;
        int requestsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<CompletableFuture<Long>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                long totalTime = 0;
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        long startTime = System.currentTimeMillis();
                        
                        LoginRequest loginRequest = new LoginRequest();
                        loginRequest.setUsername("testuser");
                        loginRequest.setPassword("password123");

                        mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk());

                        long endTime = System.currentTimeMillis();
                        totalTime += (endTime - startTime);
                    } catch (Exception e) {
                        fail("Login request failed: " + e.getMessage());
                    }
                }
                return totalTime;
            }, executor);
            futures.add(future);
        }

        // Wait for all requests to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        allFutures.get(30, TimeUnit.SECONDS);

        // Calculate average response time
        long totalTime = futures.stream()
                .mapToLong(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();

        double averageTime = (double) totalTime / (numberOfThreads * requestsPerThread);
        
        System.out.println("Concurrent login test completed:");
        System.out.println("Total requests: " + (numberOfThreads * requestsPerThread));
        System.out.println("Average response time: " + averageTime + "ms");
        
        // Assert that average response time is reasonable (less than 1 second)
        assertTrue(averageTime < 1000, "Average response time should be less than 1000ms");
        
        executor.shutdown();
    }

    @Test
    void testLargeDatasetPerformance() throws Exception {
        // Create a large number of cash flows
        int numberOfCashFlows = 1000;
        List<CashFlow> cashFlows = new ArrayList<>();
        
        for (int i = 0; i < numberOfCashFlows; i++) {
            CashFlow cashFlow = new CashFlow();
            cashFlow.setDescription("Test Cash Flow " + i);
            cashFlow.setAmount(new BigDecimal("100.00"));
            cashFlow.setTransactionDate(LocalDate.now().minusDays(i % 365));
            cashFlow.setType(i % 2 == 0 ? CashFlow.CashFlowType.INCOME : CashFlow.CashFlowType.EXPENSE);
            cashFlow.setCategory(testCategory);
            cashFlow.setUser(testUser);
            cashFlow.setIsActive(true);
            cashFlows.add(cashFlow);
        }
        
        // Batch save for better performance
        cashFlowRepository.saveAll(cashFlows);
        
        // Test GET /auth/cashflow performance
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/auth/cashflow")
                .header("Authorization", "Bearer " + authToken)
                .param("size", "20"))
                .andExpect(status().isOk());
        
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        System.out.println("Large dataset test completed:");
        System.out.println("Number of cash flows: " + numberOfCashFlows);
        System.out.println("Response time: " + responseTime + "ms");
        
        // Assert that response time is reasonable (less than 2 seconds)
        assertTrue(responseTime < 2000, "Response time should be less than 2000ms for large dataset");
    }

    @Test
    void testConcurrentCashFlowCreation() throws Exception {
        int numberOfThreads = 5;
        int requestsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        String requestJson = String.format(
                            "{\"description\":\"Test %d-%d\",\"amount\":100.00,\"transactionDate\":\"%s\",\"type\":\"EXPENSE\",\"categoryId\":%d}",
                            threadId, j, LocalDate.now().toString(), testCategory.getId()
                        );

                        mockMvc.perform(post("/auth/cashflow")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isCreated());
                    } catch (Exception e) {
                        return false;
                    }
                }
                return true;
            }, executor);
            futures.add(future);
        }

        // Wait for all requests to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        allFutures.get(30, TimeUnit.SECONDS);

        // Verify all requests succeeded
        boolean allSuccessful = futures.stream()
                .allMatch(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        return false;
                    }
                });

        assertTrue(allSuccessful, "All concurrent cash flow creation requests should succeed");
        
        // Verify the correct number of cash flows were created
        long totalCashFlows = cashFlowRepository.count();
        assertEquals(numberOfThreads * requestsPerThread, totalCashFlows, 
                "Total cash flows should match the number of requests");
        
        executor.shutdown();
    }

    @Test
    void testMemoryUsageWithLargeDataset() throws Exception {
        // Create a very large dataset to test memory usage
        int numberOfCashFlows = 5000;
        List<CashFlow> cashFlows = new ArrayList<>();
        
        for (int i = 0; i < numberOfCashFlows; i++) {
            CashFlow cashFlow = new CashFlow();
            cashFlow.setDescription("Test Cash Flow " + i);
            cashFlow.setAmount(new BigDecimal("100.00"));
            cashFlow.setTransactionDate(LocalDate.now().minusDays(i % 365));
            cashFlow.setType(i % 2 == 0 ? CashFlow.CashFlowType.INCOME : CashFlow.CashFlowType.EXPENSE);
            cashFlow.setCategory(testCategory);
            cashFlow.setUser(testUser);
            cashFlow.setIsActive(true);
            cashFlows.add(cashFlow);
        }
        
        // Batch save
        cashFlowRepository.saveAll(cashFlows);
        
        // Test memory usage during retrieval
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // Perform multiple operations
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/auth/cashflow")
                    .header("Authorization", "Bearer " + authToken)
                    .param("size", "100"))
                    .andExpect(status().isOk());
        }
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        System.out.println("Memory usage test completed:");
        System.out.println("Number of cash flows: " + numberOfCashFlows);
        System.out.println("Memory used: " + (memoryUsed / 1024 / 1024) + " MB");
        
        // Assert that memory usage is reasonable (less than 100MB)
        assertTrue(memoryUsed < 100 * 1024 * 1024, "Memory usage should be less than 100MB");
    }

    @Test
    void testSearchPerformance() throws Exception {
        // Create cash flows with searchable descriptions
        int numberOfCashFlows = 500;
        List<CashFlow> cashFlows = new ArrayList<>();
        
        String[] searchTerms = {"grocery", "shopping", "food", "restaurant", "gas"};
        
        for (int i = 0; i < numberOfCashFlows; i++) {
            CashFlow cashFlow = new CashFlow();
            String searchTerm = searchTerms[i % searchTerms.length];
            cashFlow.setDescription(searchTerm + " transaction " + i);
            cashFlow.setAmount(new BigDecimal("100.00"));
            cashFlow.setTransactionDate(LocalDate.now().minusDays(i % 30));
            cashFlow.setType(CashFlow.CashFlowType.EXPENSE);
            cashFlow.setCategory(testCategory);
            cashFlow.setUser(testUser);
            cashFlow.setIsActive(true);
            cashFlows.add(cashFlow);
        }
        
        cashFlowRepository.saveAll(cashFlows);
        
        // Test search performance
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/auth/cashflow/search")
                .header("Authorization", "Bearer " + authToken)
                .param("term", "grocery"))
                .andExpect(status().isOk());
        
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        System.out.println("Search performance test completed:");
        System.out.println("Number of cash flows: " + numberOfCashFlows);
        System.out.println("Search response time: " + responseTime + "ms");
        
        // Assert that search response time is reasonable (less than 1 second)
        assertTrue(responseTime < 1000, "Search response time should be less than 1000ms");
    }
}
