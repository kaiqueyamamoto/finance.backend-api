package com.finance.finance.repository;

import com.finance.finance.entity.CashFlow;
import com.finance.finance.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CashFlowRepository extends JpaRepository<CashFlow, Long> {
    
    Page<CashFlow> findByUserAndIsActiveTrue(User user, Pageable pageable);
    
    List<CashFlow> findByUserAndTypeAndIsActiveTrue(User user, CashFlow.CashFlowType type);
    
    List<CashFlow> findByUserAndTransactionDateBetweenAndIsActiveTrue(
        User user, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT cf FROM CashFlow cf WHERE cf.user = :user AND cf.isActive = true AND " +
           "cf.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY cf.transactionDate DESC, cf.createdAt DESC")
    List<CashFlow> findByUserAndDateRangeOrdered(
        @Param("user") User user, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT cf FROM CashFlow cf WHERE cf.user = :user AND cf.isActive = true AND " +
           "cf.type = :type AND cf.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY cf.transactionDate DESC")
    List<CashFlow> findByUserAndTypeAndDateRange(
        @Param("user") User user, 
        @Param("type") CashFlow.CashFlowType type,
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(cf.amount) FROM CashFlow cf WHERE cf.user = :user AND " +
           "cf.type = :type AND cf.transactionDate BETWEEN :startDate AND :endDate " +
           "AND cf.isActive = true")
    BigDecimal sumAmountByUserAndTypeAndDateRange(
        @Param("user") User user,
        @Param("type") CashFlow.CashFlowType type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT cf FROM CashFlow cf WHERE cf.user = :user AND cf.isActive = true AND " +
           "(LOWER(cf.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cf.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY cf.transactionDate DESC")
    List<CashFlow> findByUserAndSearchTerm(
        @Param("user") User user, 
        @Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(cf) FROM CashFlow cf WHERE cf.user = :user AND cf.type = :type " +
           "AND cf.transactionDate BETWEEN :startDate AND :endDate AND cf.isActive = true")
    long countByUserAndTypeAndDateRange(
        @Param("user") User user,
        @Param("type") CashFlow.CashFlowType type,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
    
    List<CashFlow> findByUserAndIsRecurringTrueAndIsActiveTrue(User user);
}
