package com.metron.dto;

import com.metron.model.entity.Income;
import com.metron.model.entity.IncomeType;
import com.metron.model.entity.ReceiptStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class IncomeResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private CategoryDto category;
    private LocalDate receiptDate;
    private IncomeType incomeType;
    private ReceiptStatus receiptStatus;
    private String notes;
    private LocalDateTime createdAt;

    public IncomeResponse() {}

    public IncomeResponse(Income income) {
        if (income != null) {
            this.id = income.getId();
            this.description = income.getDescription();
            this.amount = income.getAmount();
            if (income.getCategory() != null) {
                this.category = new CategoryDto(income.getCategory());
            }
            this.receiptDate = income.getReceiptDate();
            this.incomeType = income.getIncomeType();
            this.receiptStatus = income.getReceiptStatus();
            this.notes = income.getNotes();
            this.createdAt = income.getCreatedAt();
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

    public LocalDate getReceiptDate() { return receiptDate; }
    public void setReceiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; }

    public IncomeType getIncomeType() { return incomeType; }
    public void setIncomeType(IncomeType incomeType) { this.incomeType = incomeType; }

    public ReceiptStatus getReceiptStatus() { return receiptStatus; }
    public void setReceiptStatus(ReceiptStatus receiptStatus) { this.receiptStatus = receiptStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
