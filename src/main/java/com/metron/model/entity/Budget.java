package com.metron.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_month_year", columnNames = {"user_id", "budget_month", "budget_year"})
})
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "budget_month", nullable = false)
    private Integer month;

    @Column(name = "budget_year", nullable = false)
    private Integer year;

    @Column(name = "expected_income", nullable = false, precision = 15, scale = 2)
    private BigDecimal expectedIncome = BigDecimal.ZERO;

    @Column(name = "expected_extra_income", nullable = false, precision = 15, scale = 2)
    private BigDecimal expectedExtraIncome = BigDecimal.ZERO;

    @Column(name = "fixed_expenses_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal fixedExpensesBudget = BigDecimal.ZERO;

    @Column(name = "variable_expenses_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal variableExpensesBudget = BigDecimal.ZERO;

    @Column(name = "savings_target", nullable = false, precision = 15, scale = 2)
    private BigDecimal savingsTarget = BigDecimal.ZERO;

    @Column(name = "salary_expected_date")
    private LocalDate salaryExpectedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.expectedIncome == null) this.expectedIncome = BigDecimal.ZERO;
        if (this.expectedExtraIncome == null) this.expectedExtraIncome = BigDecimal.ZERO;
        if (this.fixedExpensesBudget == null) this.fixedExpensesBudget = BigDecimal.ZERO;
        if (this.variableExpensesBudget == null) this.variableExpensesBudget = BigDecimal.ZERO;
        if (this.savingsTarget == null) this.savingsTarget = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Budget() {}

    public Budget(User user, Integer month, Integer year, BigDecimal expectedIncome, BigDecimal expectedExtraIncome,
                  BigDecimal fixedExpensesBudget, BigDecimal variableExpensesBudget, BigDecimal savingsTarget, LocalDate salaryExpectedDate) {
        this.user = user;
        this.month = month;
        this.year = year;
        this.expectedIncome = expectedIncome != null ? expectedIncome : BigDecimal.ZERO;
        this.expectedExtraIncome = expectedExtraIncome != null ? expectedExtraIncome : BigDecimal.ZERO;
        this.fixedExpensesBudget = fixedExpensesBudget != null ? fixedExpensesBudget : BigDecimal.ZERO;
        this.variableExpensesBudget = variableExpensesBudget != null ? variableExpensesBudget : BigDecimal.ZERO;
        this.savingsTarget = savingsTarget != null ? savingsTarget : BigDecimal.ZERO;
        this.salaryExpectedDate = salaryExpectedDate;
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

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal getExpectedIncome() {
        return expectedIncome;
    }

    public void setExpectedIncome(BigDecimal expectedIncome) {
        this.expectedIncome = expectedIncome;
    }

    public BigDecimal getExpectedExtraIncome() {
        return expectedExtraIncome;
    }

    public void setExpectedExtraIncome(BigDecimal expectedExtraIncome) {
        this.expectedExtraIncome = expectedExtraIncome;
    }

    public BigDecimal getFixedExpensesBudget() {
        return fixedExpensesBudget;
    }

    public void setFixedExpensesBudget(BigDecimal fixedExpensesBudget) {
        this.fixedExpensesBudget = fixedExpensesBudget;
    }

    public BigDecimal getVariableExpensesBudget() {
        return variableExpensesBudget;
    }

    public void setVariableExpensesBudget(BigDecimal variableExpensesBudget) {
        this.variableExpensesBudget = variableExpensesBudget;
    }

    public BigDecimal getSavingsTarget() {
        return savingsTarget;
    }

    public void setSavingsTarget(BigDecimal savingsTarget) {
        this.savingsTarget = savingsTarget;
    }

    public LocalDate getSalaryExpectedDate() {
        return salaryExpectedDate;
    }

    public void setSalaryExpectedDate(LocalDate salaryExpectedDate) {
        this.salaryExpectedDate = salaryExpectedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
