package com.finance.finance.repository;

import com.finance.finance.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByTypeAndIsActiveTrue(Category.CategoryType type);
    
    List<Category> findByIsActiveTrue();
    
    Optional<Category> findByNameAndType(String name, Category.CategoryType type);
    
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Category> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    boolean existsByNameAndType(String name, Category.CategoryType type);
    
    @Query("SELECT COUNT(c) FROM Category c WHERE c.type = :type AND c.isActive = true")
    long countByTypeAndActive(@Param("type") Category.CategoryType type);
}
