package com.finance.finance.util;

import com.finance.finance.entity.CashFlow;
import com.finance.finance.entity.Category;
import com.finance.finance.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestDataFactory {

    public static User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setWhatsapp("123456789");
        user.setRoles("USER");
        user.setEnabled(true);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    public static User createTestUser(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRoles("USER");
        user.setEnabled(true);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    public static Category createTestCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        category.setDescription("Test category description");
        category.setType(Category.CategoryType.EXPENSE);
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

    public static Category createTestCategory(Long id, String name, Category.CategoryType type) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription("Test category description");
        category.setType(type);
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

    public static CashFlow createTestCashFlow() {
        CashFlow cashFlow = new CashFlow();
        cashFlow.setId(1L);
        cashFlow.setDescription("Test Cash Flow");
        cashFlow.setAmount(new BigDecimal("100.00"));
        cashFlow.setTransactionDate(LocalDate.now());
        cashFlow.setType(CashFlow.CashFlowType.EXPENSE);
        cashFlow.setNotes("Test notes");
        cashFlow.setIsRecurring(false);
        cashFlow.setIsActive(true);
        cashFlow.setCreatedAt(LocalDateTime.now());
        cashFlow.setUpdatedAt(LocalDateTime.now());
        return cashFlow;
    }

    public static CashFlow createTestCashFlow(Long id, String description, BigDecimal amount, 
                                            CashFlow.CashFlowType type, User user, Category category) {
        CashFlow cashFlow = new CashFlow();
        cashFlow.setId(id);
        cashFlow.setDescription(description);
        cashFlow.setAmount(amount);
        cashFlow.setTransactionDate(LocalDate.now());
        cashFlow.setType(type);
        cashFlow.setUser(user);
        cashFlow.setCategory(category);
        cashFlow.setNotes("Test notes");
        cashFlow.setIsRecurring(false);
        cashFlow.setIsActive(true);
        cashFlow.setCreatedAt(LocalDateTime.now());
        cashFlow.setUpdatedAt(LocalDateTime.now());
        return cashFlow;
    }
}
