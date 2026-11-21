package com.metron.dto;

import com.metron.model.entity.PaymentMethod;
import com.metron.model.entity.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseRequest {

    @NotBlank(message = "A descrição é obrigatória.")
    private String description;

    @NotNull(message = "O valor é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
    private BigDecimal amount;

    private Long categoryId;

    @NotNull(message = "A data da despesa é obrigatória.")
    private LocalDate expenseDate;

    @NotNull(message = "A forma de pagamento é obrigatória.")
    private PaymentMethod paymentMethod;

    @NotNull(message = "O status do pagamento é obrigatório.")
    private PaymentStatus paymentStatus;

    @com.fasterxml.jackson.annotation.JsonProperty("isRecurring")
    private boolean isRecurring = false;
    private String notes;

    public ExpenseRequest() {}

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

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
}
