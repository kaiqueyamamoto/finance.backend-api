package com.finance.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.finance.dto.CashFlowRequest;
import com.finance.finance.dto.CashFlowResponse;
import com.finance.finance.entity.CashFlow;
import com.finance.finance.entity.Category;
import com.finance.finance.entity.User;
import com.finance.finance.service.CashFlowService;
import com.finance.finance.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CashFlowController.class)
class CashFlowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CashFlowService cashFlowService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private CashFlow testCashFlow;
    private CashFlowRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser();
        testCashFlow = TestDataFactory.createTestCashFlow();
        
        testRequest = new CashFlowRequest();
        testRequest.setDescription("Test Cash Flow");
        testRequest.setAmount(new BigDecimal("100.00"));
        testRequest.setTransactionDate(LocalDate.now());
        testRequest.setType(CashFlow.CashFlowType.EXPENSE);
        testRequest.setCategoryId(1L);
        testRequest.setNotes("Test notes");
    }

    @Test
    @WithMockUser
    void createCashFlow_ValidRequest_ShouldReturnCreated() throws Exception {
        // Given
        CashFlowResponse response = CashFlowResponse.fromEntity(testCashFlow);
        when(cashFlowService.createCashFlow(any(CashFlowRequest.class), any(User.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/auth/cashflow")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value(testCashFlow.getDescription()))
                .andExpect(jsonPath("$.amount").value(testCashFlow.getAmount()));
    }

    @Test
    @WithMockUser
    void getCashFlow_ValidId_ShouldReturnOk() throws Exception {
        // Given
        CashFlowResponse response = CashFlowResponse.fromEntity(testCashFlow);
        when(cashFlowService.getCashFlowById(anyLong(), any(User.class)))
                .thenReturn(Optional.of(response));

        // When & Then
        mockMvc.perform(get("/auth/cashflow/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCashFlow.getId()))
                .andExpect(jsonPath("$.description").value(testCashFlow.getDescription()));
    }

    @Test
    @WithMockUser
    void getCashFlow_InvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        when(cashFlowService.getCashFlowById(anyLong(), any(User.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/auth/cashflow/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateCashFlow_ValidRequest_ShouldReturnOk() throws Exception {
        // Given
        CashFlowResponse response = CashFlowResponse.fromEntity(testCashFlow);
        when(cashFlowService.updateCashFlow(anyLong(), any(CashFlowRequest.class), any(User.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(put("/auth/cashflow/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCashFlow.getId()));
    }

    @Test
    @WithMockUser
    void deleteCashFlow_ValidId_ShouldReturnNoContent() throws Exception {
        // Given
        when(cashFlowService.deleteCashFlow(anyLong(), any(User.class)))
                .thenReturn(null);

        // When & Then
        mockMvc.perform(delete("/auth/cashflow/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void getCashFlows_ShouldReturnOk() throws Exception {
        // Given
        List<CashFlowResponse> responses = List.of(CashFlowResponse.fromEntity(testCashFlow));
        when(cashFlowService.getCashFlows(any(User.class), any()))
                .thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/auth/cashflow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testCashFlow.getId()));
    }

    @Test
    void createCashFlow_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/cashflow")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isUnauthorized());
    }
}
