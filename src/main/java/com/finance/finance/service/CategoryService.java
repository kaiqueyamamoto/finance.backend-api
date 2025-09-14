package com.finance.finance.service;

import com.finance.finance.dto.CategoryResponse;
import com.finance.finance.entity.Category;
import com.finance.finance.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public CategoryResponse createCategory(String name, String description, Category.CategoryType type) {
        if (categoryRepository.existsByNameAndType(name, type)) {
            throw new RuntimeException("Category with this name and type already exists");
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setType(type);
        category.setIsActive(true);

        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.fromEntity(savedCategory);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByIsActiveTrue()
                .stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getCategoriesByType(Category.CategoryType type) {
        return categoryRepository.findByTypeAndIsActiveTrue(type)
                .stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<CategoryResponse> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .filter(Category::getIsActive)
                .map(CategoryResponse::fromEntity);
    }

    public List<CategoryResponse> searchCategories(String searchTerm) {
        return categoryRepository.findBySearchTerm(searchTerm)
                .stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public CategoryResponse updateCategory(Long id, String name, String description, Category.CategoryType type) {
        Category category = categoryRepository.findById(id)
                .filter(Category::getIsActive)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Check if another category with the same name and type exists
        if (!category.getName().equals(name) && categoryRepository.existsByNameAndType(name, type)) {
            throw new RuntimeException("Category with this name and type already exists");
        }

        category.setName(name);
        category.setDescription(description);
        category.setType(type);

        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.fromEntity(savedCategory);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .filter(Category::getIsActive)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setIsActive(false);
        categoryRepository.save(category);
    }

    public long getCategoryCountByType(Category.CategoryType type) {
        return categoryRepository.countByTypeAndActive(type);
    }

    public void initializeDefaultCategories() {
        // Initialize default income categories
        if (categoryRepository.countByTypeAndActive(Category.CategoryType.INCOME) == 0) {
            createCategory("Salário", "Salário mensal", Category.CategoryType.INCOME);
            createCategory("Freelance", "Trabalhos como freelancer", Category.CategoryType.INCOME);
            createCategory("Investimentos", "Rendimentos de investimentos", Category.CategoryType.INCOME);
            createCategory("Vendas", "Venda de produtos ou serviços", Category.CategoryType.INCOME);
            createCategory("Outros", "Outras receitas", Category.CategoryType.INCOME);
        }

        // Initialize default expense categories
        if (categoryRepository.countByTypeAndActive(Category.CategoryType.EXPENSE) == 0) {
            createCategory("Alimentação", "Gastos com comida e bebida", Category.CategoryType.EXPENSE);
            createCategory("Transporte", "Gastos com transporte", Category.CategoryType.EXPENSE);
            createCategory("Moradia", "Aluguel, financiamento, condomínio", Category.CategoryType.EXPENSE);
            createCategory("Saúde", "Gastos com saúde e medicamentos", Category.CategoryType.EXPENSE);
            createCategory("Educação", "Cursos, livros, educação", Category.CategoryType.EXPENSE);
            createCategory("Lazer", "Entretenimento e lazer", Category.CategoryType.EXPENSE);
            createCategory("Roupas", "Gastos com vestuário", Category.CategoryType.EXPENSE);
            createCategory("Outros", "Outras despesas", Category.CategoryType.EXPENSE);
        }
    }
}
