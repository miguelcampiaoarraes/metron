package com.metron.dto;

import com.metron.model.entity.User;
import java.math.BigDecimal;

public class UserDto {

    private Long id;
    private String name;
    private String email;
    private BigDecimal monthlyIncome;
    private BigDecimal currentBankBalance;
    private BigDecimal savingsGoalMonthly;
    private Integer salaryDay;

    public UserDto() {}

    public UserDto(User user) {
        if (user != null) {
            this.id = user.getId();
            this.name = user.getName();
            this.email = user.getEmail();
            this.monthlyIncome = user.getMonthlyIncome();
            this.currentBankBalance = user.getCurrentBankBalance();
            this.savingsGoalMonthly = user.getSavingsGoalMonthly();
            this.salaryDay = user.getSalaryDay();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public BigDecimal getCurrentBankBalance() { return currentBankBalance; }
    public void setCurrentBankBalance(BigDecimal currentBankBalance) { this.currentBankBalance = currentBankBalance; }

    public BigDecimal getSavingsGoalMonthly() { return savingsGoalMonthly; }
    public void setSavingsGoalMonthly(BigDecimal savingsGoalMonthly) { this.savingsGoalMonthly = savingsGoalMonthly; }

    public Integer getSalaryDay() { return salaryDay; }
    public void setSalaryDay(Integer salaryDay) { this.salaryDay = salaryDay; }
}
