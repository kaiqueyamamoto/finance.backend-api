package com.finance.finance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.finance.dto.LoginRequest;
import com.finance.finance.entity.Category;
import com.finance.finance.entity.User;
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

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class CategoryIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean up test data
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test user
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRoles("USER");
        testUser.setEnabled(true);
        userRepository.save(testUser);

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
    void testCreateCategory() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("name", "Test Category");
        request.put("description", "Test Description");
        request.put("type", "EXPENSE");

        mockMvc.perform(post("/auth/categories")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Category"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void testCreateCategoryWithoutAuth() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("name", "Test Category");
        request.put("description", "Test Description");
        request.put("type", "EXPENSE");

        mockMvc.perform(post("/auth/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateCategoryWithInvalidType() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("name", "Test Category");
        request.put("description", "Test Description");
        request.put("type", "INVALID_TYPE");

        mockMvc.perform(post("/auth/categories")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllCategories() throws Exception {
        // Create test categories
        createTestCategory("Category 1", "Description 1", Category.CategoryType.INCOME);
        createTestCategory("Category 2", "Description 2", Category.CategoryType.EXPENSE);

        mockMvc.perform(get("/auth/categories")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].type").exists());
    }

    @Test
    void testGetCategoriesByType() throws Exception {
        // Create test categories
        createTestCategory("Income Category", "Income Description", Category.CategoryType.INCOME);
        createTestCategory("Expense Category", "Expense Description", Category.CategoryType.EXPENSE);

        mockMvc.perform(get("/auth/categories/type/INCOME")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("INCOME"))
                .andExpect(jsonPath("$[0].name").value("Income Category"));
    }

    @Test
    void testGetCategoryById() throws Exception {
        Category category = createTestCategory("Test Category", "Test Description", Category.CategoryType.EXPENSE);

        mockMvc.perform(get("/auth/categories/" + category.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId().intValue()))
                .andExpect(jsonPath("$.name").value("Test Category"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    void testGetCategoryByIdNotFound() throws Exception {
        mockMvc.perform(get("/auth/categories/999")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchCategories() throws Exception {
        // Create test categories
        createTestCategory("Grocery Shopping", "Food and groceries", Category.CategoryType.EXPENSE);
        createTestCategory("Gas Station", "Fuel expenses", Category.CategoryType.EXPENSE);
        createTestCategory("Salary", "Monthly salary", Category.CategoryType.INCOME);

        mockMvc.perform(get("/auth/categories/search")
                .header("Authorization", "Bearer " + authToken)
                .param("term", "grocery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Grocery Shopping"));
    }

    @Test
    void testUpdateCategory() throws Exception {
        Category category = createTestCategory("Original Name", "Original Description", Category.CategoryType.EXPENSE);

        Map<String, String> updateRequest = new HashMap<>();
        updateRequest.put("name", "Updated Name");
        updateRequest.put("description", "Updated Description");
        updateRequest.put("type", "INCOME");

        mockMvc.perform(put("/auth/categories/" + category.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    void testUpdateCategoryNotFound() throws Exception {
        Map<String, String> updateRequest = new HashMap<>();
        updateRequest.put("name", "Updated Name");
        updateRequest.put("description", "Updated Description");
        updateRequest.put("type", "INCOME");

        mockMvc.perform(put("/auth/categories/999")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteCategory() throws Exception {
        Category category = createTestCategory("To be deleted", "Will be deleted", Category.CategoryType.EXPENSE);

        mockMvc.perform(delete("/auth/categories/" + category.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get("/auth/categories/" + category.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteCategoryNotFound() throws Exception {
        mockMvc.perform(delete("/auth/categories/999")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testInitializeDefaultCategories() throws Exception {
        mockMvc.perform(post("/auth/categories/initialize")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Default categories initialized successfully"));

        // Verify categories were created
        mockMvc.perform(get("/auth/categories")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    void testGetCategoryStats() throws Exception {
        // Create test categories
        createTestCategory("Income 1", "Income Description", Category.CategoryType.INCOME);
        createTestCategory("Income 2", "Income Description", Category.CategoryType.INCOME);
        createTestCategory("Expense 1", "Expense Description", Category.CategoryType.EXPENSE);

        mockMvc.perform(get("/auth/categories/stats")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incomeCategories").value(2))
                .andExpect(jsonPath("$.expenseCategories").value(1))
                .andExpect(jsonPath("$.totalCategories").value(3));
    }

    @Test
    void testCreateCategoryWithEmptyName() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("name", "");
        request.put("description", "Test Description");
        request.put("type", "EXPENSE");

        mockMvc.perform(post("/auth/categories")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCategoryWithLongName() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("name", "A".repeat(101)); // Too long
        request.put("description", "Test Description");
        request.put("type", "EXPENSE");

        mockMvc.perform(post("/auth/categories")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private Category createTestCategory(String name, String description, Category.CategoryType type) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setType(type);
        category.setIsActive(true);
        return categoryRepository.save(category);
    }
}
