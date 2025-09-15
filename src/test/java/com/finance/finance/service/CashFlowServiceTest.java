package com.finance.finance.service;

import com.finance.finance.dto.CashFlowRequest;
import com.finance.finance.dto.CashFlowResponse;
import com.finance.finance.entity.CashFlow;
import com.finance.finance.entity.Category;
import com.finance.finance.entity.User;
import com.finance.finance.repository.CashFlowRepository;
import com.finance.finance.repository.CategoryRepository;
import com.finance.finance.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashFlowServiceTest {

    @Mock
    private CashFlowRepository cashFlowRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CashFlowService cashFlowService;

    private User testUser;
    private Category testCategory;
    private CashFlow testCashFlow;
    private CashFlowRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser();
        testCategory = TestDataFactory.createTestCategory();
        testCashFlow = TestDataFactory.createTestCashFlow(1L, "Test Cash Flow", 
                new BigDecimal("100.00"), CashFlow.CashFlowType.EXPENSE, testUser, testCategory);
        
        testRequest = new CashFlowRequest();
        testRequest.setDescription("Test Cash Flow");
        testRequest.setAmount(new BigDecimal("100.00"));
        testRequest.setTransactionDate(LocalDate.now());
        testRequest.setType(CashFlow.CashFlowType.EXPENSE);
        testRequest.setCategoryId(1L);
        testRequest.setNotes("Test notes");
    }

    @Test
    void createCashFlow_ValidRequest_ShouldReturnResponse() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(cashFlowRepository.save(any(CashFlow.class))).thenReturn(testCashFlow);

        // When
        CashFlowResponse response = cashFlowService.createCashFlow(testRequest, testUser);

        // Then
        assertNotNull(response);
        assertEquals(testCashFlow.getDescription(), response.getDescription());
        assertEquals(testCashFlow.getAmount(), response.getAmount());
        verify(cashFlowRepository).save(any(CashFlow.class));
    }

    @Test
    void createCashFlow_CategoryNotFound_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                cashFlowService.createCashFlow(testRequest, testUser));
    }

    @Test
    void getCashFlowById_ValidId_ShouldReturnResponse() {
        // Given
        when(cashFlowRepository.findById(1L)).thenReturn(Optional.of(testCashFlow));

        // When
        Optional<CashFlowResponse> response = cashFlowService.getCashFlowById(1L, testUser);

        // Then
        assertTrue(response.isPresent());
        assertEquals(testCashFlow.getId(), response.get().getId());
    }

    @Test
    void getCashFlowById_InvalidId_ShouldReturnEmpty() {
        // Given
        when(cashFlowRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<CashFlowResponse> response = cashFlowService.getCashFlowById(1L, testUser);

        // Then
        assertTrue(response.isEmpty());
    }

    @Test
    void updateCashFlow_ValidRequest_ShouldReturnResponse() {
        // Given
        when(cashFlowRepository.findById(1L)).thenReturn(Optional.of(testCashFlow));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(cashFlowRepository.save(any(CashFlow.class))).thenReturn(testCashFlow);

        // When
        CashFlowResponse response = cashFlowService.updateCashFlow(1L, testRequest, testUser);

        // Then
        assertNotNull(response);
        verify(cashFlowRepository).save(any(CashFlow.class));
    }

    @Test
    void updateCashFlow_CashFlowNotFound_ShouldThrowException() {
        // Given
        when(cashFlowRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                cashFlowService.updateCashFlow(1L, testRequest, testUser));
    }

    @Test
    void deleteCashFlow_ValidId_ShouldDelete() {
        // Given
        when(cashFlowRepository.findById(1L)).thenReturn(Optional.of(testCashFlow));

        // When
        cashFlowService.deleteCashFlow(1L, testUser);

        // Then
        verify(cashFlowRepository).delete(testCashFlow);
    }

    @Test
    void deleteCashFlow_CashFlowNotFound_ShouldThrowException() {
        // Given
        when(cashFlowRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                cashFlowService.deleteCashFlow(1L, testUser));
    }

    @Test
    void getCashFlows_ShouldReturnPage() {
        // Given
        Page<CashFlow> page = new PageImpl<>(List.of(testCashFlow));
        when(cashFlowRepository.findByUserAndIsActiveTrue(any(User.class), any(Pageable.class)))
                .thenReturn(page);

        // When
        List<CashFlowResponse> responses = cashFlowService.getCashFlows(testUser, Pageable.unpaged());

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testCashFlow.getId(), responses.get(0).getId());
    }

    @Test
    void getTotalIncome_ShouldReturnSum() {
        // Given
        when(cashFlowRepository.sumAmountByUserAndTypeAndDateRange(
                any(User.class), any(CashFlow.CashFlowType.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1000.00"));

        // When
        BigDecimal total = cashFlowService.getTotalIncome(testUser, LocalDate.now(), LocalDate.now());

        // Then
        assertEquals(new BigDecimal("1000.00"), total);
    }

    @Test
    void getTotalExpenses_ShouldReturnSum() {
        // Given
        when(cashFlowRepository.sumAmountByUserAndTypeAndDateRange(
                any(User.class), any(CashFlow.CashFlowType.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("500.00"));

        // When
        BigDecimal total = cashFlowService.getTotalExpenses(testUser, LocalDate.now(), LocalDate.now());

        // Then
        assertEquals(new BigDecimal("500.00"), total);
    }

    @Test
    void getBalance_ShouldReturnDifference() {
        // Given
        when(cashFlowRepository.sumAmountByUserAndTypeAndDateRange(
                any(User.class), eq(CashFlow.CashFlowType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1000.00"));
        when(cashFlowRepository.sumAmountByUserAndTypeAndDateRange(
                any(User.class), eq(CashFlow.CashFlowType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("500.00"));

        // When
        BigDecimal balance = cashFlowService.getBalance(testUser, LocalDate.now(), LocalDate.now());

        // Then
        assertEquals(new BigDecimal("500.00"), balance);
    }
}
