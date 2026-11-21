package com.metron.dto;

import com.metron.model.entity.Expense;
import com.metron.model.entity.PaymentMethod;
import com.metron.model.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpenseResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private CategoryDto category;
    private LocalDate expenseDate;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    @com.fasterxml.jackson.annotation.JsonProperty("isRecurring")
    private boolean isRecurring;
    private String notes;
    private LocalDateTime createdAt;

    public ExpenseResponse() {}

    public ExpenseResponse(Expense expense) {
        if (expense != null) {
            this.id = expense.getId();
            this.description = expense.getDescription();
            this.amount = expense.getAmount();
            if (expense.getCategory() != null) {
                this.category = new CategoryDto(expense.getCategory());
            }
            this.expenseDate = expense.getExpenseDate();
            this.paymentMethod = expense.getPaymentMethod();
            this.paymentStatus = expense.getPaymentStatus();
            this.isRecurring = expense.isRecurring();
            this.notes = expense.getNotes();
            this.createdAt = expense.getCreatedAt();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public CategoryDto getCategory() { return category; }
    public void setCategory(CategoryDto category) { this.category = category; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    @com.fasterxml.jackson.annotation.JsonProperty("isRecurring")
    public boolean isRecurring() { return isRecurring; }

    @com.fasterxml.jackson.annotation.JsonProperty("isRecurring")
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
