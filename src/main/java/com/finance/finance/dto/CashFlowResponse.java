package com.finance.finance.dto;

import com.finance.finance.entity.CashFlow;
import com.finance.finance.entity.Category;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CashFlowResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private CashFlow.CashFlowType type;
    private CategoryResponse category;
    private String notes;
    private Boolean isRecurring;
    private String recurringFrequency;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CashFlowResponse() {}

    public CashFlowResponse(Long id, String description, BigDecimal amount, 
                           LocalDate transactionDate, CashFlow.CashFlowType type,
                           CategoryResponse category, String notes, Boolean isRecurring,
                           String recurringFrequency, Boolean isActive, 
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.type = type;
        this.category = category;
        this.notes = notes;
        this.isRecurring = isRecurring;
        this.recurringFrequency = recurringFrequency;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Static factory method to create from entity
    public static CashFlowResponse fromEntity(CashFlow cashFlow) {
        CategoryResponse categoryResponse = null;
        if (cashFlow.getCategory() != null) {
            categoryResponse = CategoryResponse.fromEntity(cashFlow.getCategory());
        }

        return new CashFlowResponse(
            cashFlow.getId(),
            cashFlow.getDescription(),
            cashFlow.getAmount(),
            cashFlow.getTransactionDate(),
            cashFlow.getType(),
            categoryResponse,
            cashFlow.getNotes(),
            cashFlow.getIsRecurring(),
            cashFlow.getRecurringFrequency(),
            cashFlow.getIsActive(),
            cashFlow.getCreatedAt(),
            cashFlow.getUpdatedAt()
        );
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public CashFlow.CashFlowType getType() {
        return type;
    }

    public void setType(CashFlow.CashFlowType type) {
        this.type = type;
    }

    public CategoryResponse getCategory() {
        return category;
    }

    public void setCategory(CategoryResponse category) {
        this.category = category;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    public String getRecurringFrequency() {
        return recurringFrequency;
    }

    public void setRecurringFrequency(String recurringFrequency) {
        this.recurringFrequency = recurringFrequency;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
