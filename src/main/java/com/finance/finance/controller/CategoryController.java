package com.finance.finance.controller;

import com.finance.finance.dto.CategoryResponse;
import com.finance.finance.entity.Category;
import com.finance.finance.service.CategoryService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MeterRegistry meterRegistry;

    private final Counter categoryCreatedCounter;
    private final Counter categoryUpdatedCounter;
    private final Counter categoryDeletedCounter;

    public CategoryController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.categoryCreatedCounter = Counter.builder("finance.category.created")
                .description("Total number of categories created")
                .register(meterRegistry);
        this.categoryUpdatedCounter = Counter.builder("finance.category.updated")
                .description("Total number of categories updated")
                .register(meterRegistry);
        this.categoryDeletedCounter = Counter.builder("finance.category.deleted")
                .description("Total number of categories deleted")
                .register(meterRegistry);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String description = request.get("description");
            Category.CategoryType type = Category.CategoryType.valueOf(request.get("type"));
            
            CategoryResponse response = categoryService.createCategory(name, description, type);
            categoryCreatedCounter.increment();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByType(
            @PathVariable Category.CategoryType type) {
        List<CategoryResponse> categories = categoryService.getCategoriesByType(type);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        Optional<CategoryResponse> category = categoryService.getCategoryById(id);
        return category.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponse>> searchCategories(
            @RequestParam String term) {
        List<CategoryResponse> categories = categoryService.searchCategories(term);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String description = request.get("description");
            Category.CategoryType type = Category.CategoryType.valueOf(request.get("type"));
            
            CategoryResponse response = categoryService.updateCategory(id, name, description, type);
            categoryUpdatedCounter.increment();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            categoryDeletedCounter.increment();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/initialize")
    public ResponseEntity<Map<String, String>> initializeDefaultCategories() {
        try {
            categoryService.initializeDefaultCategories();
            return ResponseEntity.ok(Map.of("message", "Default categories initialized successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getCategoryStats() {
        long incomeCount = categoryService.getCategoryCountByType(Category.CategoryType.INCOME);
        long expenseCount = categoryService.getCategoryCountByType(Category.CategoryType.EXPENSE);
        
        return ResponseEntity.ok(Map.of(
            "incomeCategories", incomeCount,
            "expenseCategories", expenseCount,
            "totalCategories", incomeCount + expenseCount
        ));
    }
}
