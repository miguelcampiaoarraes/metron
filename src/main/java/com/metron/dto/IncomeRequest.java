package com.metron.dto;

import com.metron.model.entity.IncomeType;
import com.metron.model.entity.ReceiptStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class IncomeRequest {

    @NotBlank(message = "A descrição é obrigatória.")
    private String description;

    @NotNull(message = "O valor é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
    private BigDecimal amount;

    private Long categoryId;

    @NotNull(message = "A data de recebimento é obrigatória.")
    private LocalDate receiptDate;

    @NotNull(message = "O tipo de receita é obrigatório.")
    private IncomeType incomeType;

    @NotNull(message = "O status de recebimento é obrigatório.")
    private ReceiptStatus receiptStatus;

    private String notes;

    public IncomeRequest() {}

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public LocalDate getReceiptDate() { return receiptDate; }
    public void setReceiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; }

    public IncomeType getIncomeType() { return incomeType; }
    public void setIncomeType(IncomeType incomeType) { this.incomeType = incomeType; }

    public ReceiptStatus getReceiptStatus() { return receiptStatus; }
    public void setReceiptStatus(ReceiptStatus receiptStatus) { this.receiptStatus = receiptStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
