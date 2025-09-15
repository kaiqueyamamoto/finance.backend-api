package com.finance.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.finance.dto.CashFlowRequest;
import com.finance.finance.dto.CashFlowResponse;
import com.finance.finance.entity.CashFlow;
import com.finance.finance.service.CashFlowService;
import com.finance.finance.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestController.class)
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CashFlowService cashFlowService;

    @Autowired
    private ObjectMapper objectMapper;

    private CashFlowRequest testRequest;
    private CashFlowResponse testResponse;

    @BeforeEach
    void setUp() {
        testRequest = new CashFlowRequest();
        testRequest.setDescription("Test Cash Flow");
        testRequest.setAmount(new BigDecimal("100.00"));
        testRequest.setTransactionDate(LocalDate.now());
        testRequest.setType(CashFlow.CashFlowType.EXPENSE);
        testRequest.setCategoryId(1L);
        testRequest.setNotes("Test notes");

        CashFlow testCashFlow = TestDataFactory.createTestCashFlow();
        testResponse = CashFlowResponse.fromEntity(testCashFlow);
    }

    @Test
    void createTestUser_ShouldReturnOk() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/test/create-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Test user created (simulated)"));
    }

    @Test
    void createTestCashFlow_ValidRequest_ShouldReturnOk() throws Exception {
        // Given
        when(cashFlowService.createCashFlow(any(CashFlowRequest.class), any()))
                .thenReturn(testResponse);

        // When & Then
        mockMvc.perform(post("/auth/test/cashflow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(testResponse.getDescription()))
                .andExpect(jsonPath("$.amount").value(testResponse.getAmount()));
    }

    @Test
    void createTestCashFlow_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        when(cashFlowService.createCashFlow(any(CashFlowRequest.class), any()))
                .thenThrow(new RuntimeException("Test error"));

        // When & Then
        mockMvc.perform(post("/auth/test/cashflow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTestDashboard_ShouldReturnOk() throws Exception {
        // Given
        when(cashFlowService.getTotalIncome(any(), any(), any())).thenReturn(new BigDecimal("1000.00"));
        when(cashFlowService.getTotalExpenses(any(), any(), any())).thenReturn(new BigDecimal("500.00"));
        when(cashFlowService.getBalance(any(), any(), any())).thenReturn(new BigDecimal("500.00"));

        // When & Then
        mockMvc.perform(get("/auth/test/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(1000.00))
                .andExpect(jsonPath("$.totalExpenses").value(500.00))
                .andExpect(jsonPath("$.balance").value(500.00))
                .andExpect(jsonPath("$.startDate").exists())
                .andExpect(jsonPath("$.endDate").exists());
    }

    @Test
    void getTestDashboard_WithException_ShouldReturnBadRequest() throws Exception {
        // Given
        when(cashFlowService.getTotalIncome(any(), any(), any()))
                .thenThrow(new RuntimeException("Test error"));

        // When & Then
        mockMvc.perform(get("/auth/test/dashboard"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Test error"));
    }
}
