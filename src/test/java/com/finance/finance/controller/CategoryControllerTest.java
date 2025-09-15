package com.finance.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.finance.dto.CategoryResponse;
import com.finance.finance.entity.Category;
import com.finance.finance.service.CategoryService;
import com.finance.finance.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private Category testCategory;
    private CategoryResponse testResponse;

    @BeforeEach
    void setUp() {
        testCategory = TestDataFactory.createTestCategory();
        testResponse = CategoryResponse.fromEntity(testCategory);
    }

    @Test
    void getAllCategories_ShouldReturnOk() throws Exception {
        // Given
        List<CategoryResponse> responses = List.of(testResponse);
        when(categoryService.getAllCategories()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/auth/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testCategory.getId()))
                .andExpect(jsonPath("$[0].name").value(testCategory.getName()));
    }

    @Test
    void getCategoryById_ValidId_ShouldReturnOk() throws Exception {
        // Given
        when(categoryService.getCategoryById(anyLong())).thenReturn(Optional.of(testResponse));

        // When & Then
        mockMvc.perform(get("/auth/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCategory.getId()))
                .andExpect(jsonPath("$.name").value(testCategory.getName()));
    }

    @Test
    void getCategoryById_InvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        when(categoryService.getCategoryById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/auth/categories/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCategory_ValidRequest_ShouldReturnCreated() throws Exception {
        // Given
        Map<String, String> request = Map.of(
                "name", "New Category",
                "description", "New category description",
                "type", "EXPENSE"
        );
        when(categoryService.createCategory(anyString(), anyString(), any(Category.CategoryType.class)))
                .thenReturn(testResponse);

        // When & Then
        mockMvc.perform(post("/auth/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(testCategory.getName()));
    }

    @Test
    void updateCategory_ValidRequest_ShouldReturnOk() throws Exception {
        // Given
        Map<String, String> request = Map.of(
                "name", "Updated Category",
                "description", "Updated description",
                "type", "INCOME"
        );
        when(categoryService.updateCategory(anyLong(), anyString(), anyString(), any(Category.CategoryType.class)))
                .thenReturn(testResponse);

        // When & Then
        mockMvc.perform(put("/auth/categories/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCategory.getId()));
    }

    @Test
    void deleteCategory_ValidId_ShouldReturnNoContent() throws Exception {
        // Given
        when(categoryService.deleteCategory(anyLong())).thenReturn(null);

        // When & Then
        mockMvc.perform(delete("/auth/categories/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void searchCategories_ValidTerm_ShouldReturnOk() throws Exception {
        // Given
        List<CategoryResponse> responses = List.of(testResponse);
        when(categoryService.searchCategories(anyString())).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/auth/categories/search")
                .param("term", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value(testCategory.getName()));
    }

    @Test
    void getCategoriesByType_ValidType_ShouldReturnOk() throws Exception {
        // Given
        List<CategoryResponse> responses = List.of(testResponse);
        when(categoryService.getCategoriesByType(any(Category.CategoryType.class))).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/auth/categories/type/EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].type").value("EXPENSE"));
    }
}
