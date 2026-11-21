package com.metron.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class RegisterRequest {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres.")
    private String name;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Formato de e-mail inválido.")
    private String email;

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
    private String password;

    private BigDecimal monthlyIncome = BigDecimal.ZERO;
    private BigDecimal currentBankBalance = BigDecimal.ZERO;
    private BigDecimal savingsGoalMonthly = BigDecimal.ZERO;
    private Integer salaryDay = 5;

    public RegisterRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public BigDecimal getCurrentBankBalance() { return currentBankBalance; }
    public void setCurrentBankBalance(BigDecimal currentBankBalance) { this.currentBankBalance = currentBankBalance; }

    public BigDecimal getSavingsGoalMonthly() { return savingsGoalMonthly; }
    public void setSavingsGoalMonthly(BigDecimal savingsGoalMonthly) { this.savingsGoalMonthly = savingsGoalMonthly; }

    public Integer getSalaryDay() { return salaryDay; }
    public void setSalaryDay(Integer salaryDay) { this.salaryDay = salaryDay; }
}
