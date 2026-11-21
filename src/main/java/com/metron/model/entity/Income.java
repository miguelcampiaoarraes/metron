package com.metron.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "incomes")
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_type", nullable = false, length = 40)
    private IncomeType incomeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "receipt_status", nullable = false, length = 20)
    private ReceiptStatus receiptStatus;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.amount == null) this.amount = BigDecimal.ZERO;
        if (this.receiptStatus == null) this.receiptStatus = ReceiptStatus.RECEBIDO;
        if (this.incomeType == null) this.incomeType = IncomeType.SALARIO;
        if (this.receiptDate == null) this.receiptDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Income() {}

    public Income(User user, Category category, String description, BigDecimal amount, LocalDate receiptDate,
                  IncomeType incomeType, ReceiptStatus receiptStatus, String notes) {
        this.user = user;
        this.category = category;
        this.description = description;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.receiptDate = receiptDate != null ? receiptDate : LocalDate.now();
        this.incomeType = incomeType;
        this.receiptStatus = receiptStatus != null ? receiptStatus : ReceiptStatus.RECEBIDO;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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

    public LocalDate getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(LocalDate receiptDate) {
        this.receiptDate = receiptDate;
    }

    public IncomeType getIncomeType() {
        return incomeType;
    }

    public void setIncomeType(IncomeType incomeType) {
        this.incomeType = incomeType;
    }

    public ReceiptStatus getReceiptStatus() {
        return receiptStatus;
    }

    public void setReceiptStatus(ReceiptStatus receiptStatus) {
        this.receiptStatus = receiptStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
