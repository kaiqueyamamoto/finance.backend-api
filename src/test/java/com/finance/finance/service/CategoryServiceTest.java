package com.finance.finance.service;

import com.finance.finance.dto.CategoryResponse;
import com.finance.finance.entity.Category;
import com.finance.finance.repository.CategoryRepository;
import com.finance.finance.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = TestDataFactory.createTestCategory();
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        // Given
        List<Category> categories = List.of(testCategory);
        when(categoryRepository.findByIsActiveTrue()).thenReturn(categories);

        // When
        List<CategoryResponse> responses = categoryService.getAllCategories();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testCategory.getId(), responses.get(0).getId());
        verify(categoryRepository).findByIsActiveTrue();
    }

    @Test
    void getCategoryById_ValidId_ShouldReturnResponse() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When
        Optional<CategoryResponse> response = categoryService.getCategoryById(1L);

        // Then
        assertTrue(response.isPresent());
        assertEquals(testCategory.getId(), response.get().getId());
        assertEquals(testCategory.getName(), response.get().getName());
    }

    @Test
    void getCategoryById_InvalidId_ShouldReturnEmpty() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<CategoryResponse> response = categoryService.getCategoryById(1L);

        // Then
        assertTrue(response.isEmpty());
    }

    @Test
    void createCategory_ValidData_ShouldReturnResponse() {
        // Given
        when(categoryRepository.existsByNameAndType(anyString(), any(Category.CategoryType.class)))
                .thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        CategoryResponse response = categoryService.createCategory(
                "Test Category", "Description", Category.CategoryType.EXPENSE);

        // Then
        assertNotNull(response);
        assertEquals(testCategory.getName(), response.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_DuplicateName_ShouldThrowException() {
        // Given
        when(categoryRepository.existsByNameAndType(anyString(), any(Category.CategoryType.class)))
                .thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                categoryService.createCategory("Test Category", "Description", Category.CategoryType.EXPENSE));
    }

    @Test
    void updateCategory_ValidData_ShouldReturnResponse() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByNameAndType(anyString(), any(Category.CategoryType.class)))
                .thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        CategoryResponse response = categoryService.updateCategory(
                1L, "Updated Category", "Updated Description", Category.CategoryType.INCOME);

        // Then
        assertNotNull(response);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_CategoryNotFound_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                categoryService.updateCategory(1L, "Updated Category", "Description", Category.CategoryType.EXPENSE));
    }

    @Test
    void deleteCategory_ValidId_ShouldDelete() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When
        categoryService.deleteCategory(1L);

        // Then
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void deleteCategory_CategoryNotFound_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                categoryService.deleteCategory(1L));
    }

    @Test
    void searchCategories_ValidTerm_ShouldReturnMatching() {
        // Given
        List<Category> categories = List.of(testCategory);
        when(categoryRepository.findBySearchTerm("test")).thenReturn(categories);

        // When
        List<CategoryResponse> responses = categoryService.searchCategories("test");

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(categoryRepository).findBySearchTerm("test");
    }

    @Test
    void getCategoriesByType_ValidType_ShouldReturnMatching() {
        // Given
        List<Category> categories = List.of(testCategory);
        when(categoryRepository.findByTypeAndIsActiveTrue(Category.CategoryType.EXPENSE))
                .thenReturn(categories);

        // When
        List<CategoryResponse> responses = categoryService.getCategoriesByType(Category.CategoryType.EXPENSE);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(categoryRepository).findByTypeAndIsActiveTrue(Category.CategoryType.EXPENSE);
    }
}
