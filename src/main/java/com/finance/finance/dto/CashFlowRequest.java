package com.finance.finance.dto;

import com.finance.finance.entity.CashFlow;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CashFlowRequest {

    @NotBlank(message = "Description is required")
    @Size(min = 2, max = 200, message = "Description must be between 2 and 200 characters")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    @NotNull(message = "Type is required")
    private CashFlow.CashFlowType type;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private Boolean isRecurring = false;

    private String recurringFrequency;

    // Constructors
    public CashFlowRequest() {}

    public CashFlowRequest(String description, BigDecimal amount, LocalDate transactionDate, 
                          CashFlow.CashFlowType type, Long categoryId) {
        this.description = description;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.type = type;
        this.categoryId = categoryId;
    }

    // Getters and Setters
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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
}
