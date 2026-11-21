package com.metron.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class UpdateProfileRequest {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres.")
    private String name;

    private BigDecimal monthlyIncome;
    private BigDecimal currentBankBalance;
    private BigDecimal savingsGoalMonthly;
    private Integer salaryDay;

    private String currentPassword;
    private String newPassword;

    public UpdateProfileRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public BigDecimal getCurrentBankBalance() { return currentBankBalance; }
    public void setCurrentBankBalance(BigDecimal currentBankBalance) { this.currentBankBalance = currentBankBalance; }

    public BigDecimal getSavingsGoalMonthly() { return savingsGoalMonthly; }
    public void setSavingsGoalMonthly(BigDecimal savingsGoalMonthly) { this.savingsGoalMonthly = savingsGoalMonthly; }

    public Integer getSalaryDay() { return salaryDay; }
    public void setSalaryDay(Integer salaryDay) { this.salaryDay = salaryDay; }

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
