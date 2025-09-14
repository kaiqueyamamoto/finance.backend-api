package com.finance.finance.service;

import com.finance.finance.dto.CashFlowRequest;
import com.finance.finance.dto.CashFlowResponse;
import com.finance.finance.entity.CashFlow;
import com.finance.finance.entity.Category;
import com.finance.finance.entity.User;
import com.finance.finance.repository.CashFlowRepository;
import com.finance.finance.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CashFlowService {

    @Autowired
    private CashFlowRepository cashFlowRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public CashFlowResponse createCashFlow(CashFlowRequest request, User user) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        CashFlow cashFlow = new CashFlow();
        cashFlow.setDescription(request.getDescription());
        cashFlow.setAmount(request.getAmount());
        cashFlow.setTransactionDate(request.getTransactionDate());
        cashFlow.setType(request.getType());
        cashFlow.setCategory(category);
        cashFlow.setUser(user);
        cashFlow.setNotes(request.getNotes());
        cashFlow.setIsRecurring(request.getIsRecurring());
        cashFlow.setRecurringFrequency(request.getRecurringFrequency());

        CashFlow savedCashFlow = cashFlowRepository.save(cashFlow);
        return CashFlowResponse.fromEntity(savedCashFlow);
    }

    public Optional<CashFlowResponse> getCashFlowById(Long id, User user) {
        return cashFlowRepository.findById(id)
                .filter(cf -> cf.getUser().equals(user) && cf.getIsActive())
                .map(CashFlowResponse::fromEntity);
    }

    public Page<CashFlowResponse> getCashFlowsByUser(User user, Pageable pageable) {
        return cashFlowRepository.findByUserAndIsActiveTrue(user, pageable)
                .map(CashFlowResponse::fromEntity);
    }

    public List<CashFlowResponse> getCashFlowsByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return cashFlowRepository.findByUserAndDateRangeOrdered(user, startDate, endDate)
                .stream()
                .map(CashFlowResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CashFlowResponse> getCashFlowsByType(User user, CashFlow.CashFlowType type) {
        return cashFlowRepository.findByUserAndTypeAndIsActiveTrue(user, type)
                .stream()
                .map(CashFlowResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CashFlowResponse> searchCashFlows(User user, String searchTerm) {
        return cashFlowRepository.findByUserAndSearchTerm(user, searchTerm)
                .stream()
                .map(CashFlowResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public CashFlowResponse updateCashFlow(Long id, CashFlowRequest request, User user) {
        CashFlow cashFlow = cashFlowRepository.findById(id)
                .filter(cf -> cf.getUser().equals(user) && cf.getIsActive())
                .orElseThrow(() -> new RuntimeException("Cash flow not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        cashFlow.setDescription(request.getDescription());
        cashFlow.setAmount(request.getAmount());
        cashFlow.setTransactionDate(request.getTransactionDate());
        cashFlow.setType(request.getType());
        cashFlow.setCategory(category);
        cashFlow.setNotes(request.getNotes());
        cashFlow.setIsRecurring(request.getIsRecurring());
        cashFlow.setRecurringFrequency(request.getRecurringFrequency());

        CashFlow savedCashFlow = cashFlowRepository.save(cashFlow);
        return CashFlowResponse.fromEntity(savedCashFlow);
    }

    public void deleteCashFlow(Long id, User user) {
        CashFlow cashFlow = cashFlowRepository.findById(id)
                .filter(cf -> cf.getUser().equals(user) && cf.getIsActive())
                .orElseThrow(() -> new RuntimeException("Cash flow not found"));

        cashFlow.setIsActive(false);
        cashFlowRepository.save(cashFlow);
    }

    public BigDecimal getTotalIncome(User user, LocalDate startDate, LocalDate endDate) {
        return cashFlowRepository.sumAmountByUserAndTypeAndDateRange(
                user, CashFlow.CashFlowType.INCOME, startDate, endDate);
    }

    public BigDecimal getTotalExpenses(User user, LocalDate startDate, LocalDate endDate) {
        return cashFlowRepository.sumAmountByUserAndTypeAndDateRange(
                user, CashFlow.CashFlowType.EXPENSE, startDate, endDate);
    }

    public BigDecimal getBalance(User user, LocalDate startDate, LocalDate endDate) {
        BigDecimal income = getTotalIncome(user, startDate, endDate);
        BigDecimal expenses = getTotalExpenses(user, startDate, endDate);
        
        if (income == null) income = BigDecimal.ZERO;
        if (expenses == null) expenses = BigDecimal.ZERO;
        
        return income.subtract(expenses);
    }

    public long getTransactionCount(User user, CashFlow.CashFlowType type, 
                                   LocalDate startDate, LocalDate endDate) {
        return cashFlowRepository.countByUserAndTypeAndDateRange(user, type, startDate, endDate);
    }
}
