package com.metron.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "monthly_income", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyIncome = BigDecimal.ZERO;

    @Column(name = "current_bank_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentBankBalance = BigDecimal.ZERO;

    @Column(name = "savings_goal_monthly", nullable = false, precision = 15, scale = 2)
    private BigDecimal savingsGoalMonthly = BigDecimal.ZERO;

    @Column(name = "salary_day", nullable = false)
    private Integer salaryDay = 5;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.monthlyIncome == null) this.monthlyIncome = BigDecimal.ZERO;
        if (this.currentBankBalance == null) this.currentBankBalance = BigDecimal.ZERO;
        if (this.savingsGoalMonthly == null) this.savingsGoalMonthly = BigDecimal.ZERO;
        if (this.salaryDay == null) this.salaryDay = 5;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public User() {}

    public User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getCurrentBankBalance() {
        return currentBankBalance;
    }

    public void setCurrentBankBalance(BigDecimal currentBankBalance) {
        this.currentBankBalance = currentBankBalance;
    }

    public BigDecimal getSavingsGoalMonthly() {
        return savingsGoalMonthly;
    }

    public void setSavingsGoalMonthly(BigDecimal savingsGoalMonthly) {
        this.savingsGoalMonthly = savingsGoalMonthly;
    }

    public Integer getSalaryDay() {
        return salaryDay;
    }

    public void setSalaryDay(Integer salaryDay) {
        this.salaryDay = salaryDay;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
