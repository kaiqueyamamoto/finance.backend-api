package com.finance.finance.config;

import com.finance.finance.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryService categoryService;

    @Override
    public void run(String... args) throws Exception {
        // Initialize default categories if they don't exist
        categoryService.initializeDefaultCategories();
    }
}
