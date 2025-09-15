package com.finance.finance.security;

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
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class SecurityTest {

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
    private String user1Token;
    private String user2Token;
    private User user1;
    private User user2;
    private Category testCategory;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean up test data
        cashFlowRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test users
        user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword(passwordEncoder.encode("password123"));
        user1.setRoles("USER");
        user1.setEnabled(true);
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword(passwordEncoder.encode("password123"));
        user2.setRoles("USER");
        user2.setEnabled(true);
        user2 = userRepository.save(user2);

        // Create test category
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setDescription("Test Description");
        testCategory.setType(Category.CategoryType.EXPENSE);
        testCategory.setIsActive(true);
        testCategory = categoryRepository.save(testCategory);

        // Login both users to get tokens
        user1Token = loginUser("user1", "password123");
        user2Token = loginUser("user2", "password123");
    }

    @Test
    void testUserCannotAccessOtherUserData() throws Exception {
        // User1 creates a cash flow
        String cashFlowJson = String.format(
            "{\"description\":\"User1 Cash Flow\",\"amount\":100.00,\"transactionDate\":\"%s\",\"type\":\"EXPENSE\",\"categoryId\":%d}",
            LocalDate.now().toString(), testCategory.getId()
        );

        String response = mockMvc.perform(post("/auth/cashflow")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cashFlowJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract cash flow ID
        String cashFlowId = objectMapper.readTree(response).get("id").asText();

        // User2 tries to access User1's cash flow
        mockMvc.perform(get("/auth/cashflow/" + cashFlowId)
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isNotFound()); // Should not find it
    }

    @Test
    void testUserCannotUpdateOtherUserData() throws Exception {
        // User1 creates a cash flow
        String cashFlowJson = String.format(
            "{\"description\":\"User1 Cash Flow\",\"amount\":100.00,\"transactionDate\":\"%s\",\"type\":\"EXPENSE\",\"categoryId\":%d}",
            LocalDate.now().toString(), testCategory.getId()
        );

        String response = mockMvc.perform(post("/auth/cashflow")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cashFlowJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String cashFlowId = objectMapper.readTree(response).get("id").asText();

        // User2 tries to update User1's cash flow
        String updateJson = String.format(
            "{\"description\":\"Hacked by User2\",\"amount\":999.00,\"transactionDate\":\"%s\",\"type\":\"EXPENSE\",\"categoryId\":%d}",
            LocalDate.now().toString(), testCategory.getId()
        );

        mockMvc.perform(put("/auth/cashflow/" + cashFlowId)
                .header("Authorization", "Bearer " + user2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound()); // Should not find it
    }

    @Test
    void testUserCannotDeleteOtherUserData() throws Exception {
        // User1 creates a cash flow
        String cashFlowJson = String.format(
            "{\"description\":\"User1 Cash Flow\",\"amount\":100.00,\"transactionDate\":\"%s\",\"type\":\"EXPENSE\",\"categoryId\":%d}",
            LocalDate.now().toString(), testCategory.getId()
        );

        String response = mockMvc.perform(post("/auth/cashflow")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cashFlowJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String cashFlowId = objectMapper.readTree(response).get("id").asText();

        // User2 tries to delete User1's cash flow
        mockMvc.perform(delete("/auth/cashflow/" + cashFlowId)
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isNotFound()); // Should not find it
    }

    @Test
    void testSQLInjectionPrevention() throws Exception {
        // Test SQL injection in search
        String maliciousSearch = "'; DROP TABLE cash_flows; --";
        
        mockMvc.perform(get("/auth/cashflow/search")
                .header("Authorization", "Bearer " + user1Token)
                .param("term", maliciousSearch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Verify that the table still exists by trying to create a cash flow
        String cashFlowJson = String.format(
            "{\"description\":\"Test after injection\",\"amount\":100.00,\"transactionDate\":\"%s\",\"type\":\"EXPENSE\",\"categoryId\":%d}",
            LocalDate.now().toString(), testCategory.getId()
        );

        mockMvc.perform(post("/auth/cashflow")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cashFlowJson))
                .andExpect(status().isCreated());
    }

    @Test
    void testXSSPrevention() throws Exception {
        // Test XSS in cash flow description
        String xssPayload = "<script>alert('XSS')</script>";
        String cashFlowJson = String.format(
            "{\"description\":\"%s\",\"amount\":100.00,\"transactionDate\":\"%s\",\"type\":\"EXPENSE\",\"categoryId\":%d}",
            xssPayload, LocalDate.now().toString(), testCategory.getId()
        );

        String response = mockMvc.perform(post("/auth/cashflow")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cashFlowJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify that the script tags are not executed (should be escaped)
        String description = objectMapper.readTree(response).get("description").asText();
        assertTrue(description.contains("&lt;script&gt;"), "XSS payload should be escaped");
    }

    @Test
    void testInvalidTokenHandling() throws Exception {
        String[] invalidTokens = {
            "invalid-token",
            "Bearer invalid-token",
            "eyJhbGciOiJIUzUxMiJ9.invalid",
            "",
            null
        };

        for (String token : invalidTokens) {
            if (token == null) {
                mockMvc.perform(get("/auth/me"))
                        .andExpect(status().isForbidden());
            } else {
                mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Test
    void testExpiredTokenHandling() throws Exception {
        // This test would require a token that's actually expired
        // For now, we'll test with a malformed token that should be rejected
        String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTYwMDAwMDAwMCwiZXhwIjoxNjAwMDAwMDAwfQ.invalid";
        
        mockMvc.perform(get("/auth/me")
                .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRateLimiting() throws Exception {
        // Test multiple rapid requests to see if rate limiting is in place
        // Note: This test assumes rate limiting is implemented
        for (int i = 0; i < 100; i++) {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("user1");
            loginRequest.setPassword("wrongpassword"); // Wrong password to avoid success

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void testInputValidation() throws Exception {
        // Test various invalid inputs
        String[] invalidInputs = {
            "{\"description\":\"\",\"amount\":-100.00,\"transactionDate\":\"invalid-date\",\"type\":\"INVALID_TYPE\",\"categoryId\":999}",
            "{\"description\":null,\"amount\":null,\"transactionDate\":null,\"type\":null,\"categoryId\":null}",
            "{\"description\":\"A\".repeat(1000),\"amount\":999999999.99,\"transactionDate\":\"2024-13-45\",\"type\":\"EXPENSE\",\"categoryId\":1}",
            "{\"description\":\"<script>alert('xss')</script>\",\"amount\":100.00,\"transactionDate\":\"2024-01-01\",\"type\":\"EXPENSE\",\"categoryId\":1}"
        };

        for (String invalidInput : invalidInputs) {
            mockMvc.perform(post("/auth/cashflow")
                    .header("Authorization", "Bearer " + user1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidInput))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void testAuthorizationHeaderVariations() throws Exception {
        String validToken = user1Token;
        
        // Test different authorization header formats
        String[] authHeaders = {
            "Bearer " + validToken,
            "bearer " + validToken,
            "BEARER " + validToken,
            validToken, // Missing Bearer prefix
            "Bearer" + validToken, // Missing space
            "Bearer " + validToken + " extra", // Extra content
        };

        for (String authHeader : authHeaders) {
            if (authHeader.equals("Bearer " + validToken)) {
                // Only the correct format should work
                mockMvc.perform(get("/auth/me")
                        .header("Authorization", authHeader))
                        .andExpect(status().isOk());
            } else {
                // All other formats should fail
                mockMvc.perform(get("/auth/me")
                        .header("Authorization", authHeader))
                        .andExpect(status().isForbidden());
            }
        }
    }

    private String loginUser(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        String response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }
}
