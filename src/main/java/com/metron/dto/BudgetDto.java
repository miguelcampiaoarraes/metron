package com.metron.dto;

import com.metron.model.entity.Budget;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BudgetDto {

    private Long id;

    @NotNull(message = "O mês é obrigatório.")
    @Min(value = 1, message = "Mês inválido.")
    @Max(value = 12, message = "Mês inválido.")
    private Integer month;

    @NotNull(message = "O ano é obrigatório.")
    @Min(value = 2020, message = "Ano inválido.")
    private Integer year;

    private BigDecimal expectedIncome = BigDecimal.ZERO;
    private BigDecimal expectedExtraIncome = BigDecimal.ZERO;
    private BigDecimal fixedExpensesBudget = BigDecimal.ZERO;
    private BigDecimal variableExpensesBudget = BigDecimal.ZERO;
    private BigDecimal savingsTarget = BigDecimal.ZERO;
    private LocalDate salaryExpectedDate;

    // Computed / comparison metrics
    private BigDecimal totalExpectedIncome = BigDecimal.ZERO;
    private BigDecimal totalBudgetedExpenses = BigDecimal.ZERO;
    private BigDecimal plannedSavings = BigDecimal.ZERO;
    private BigDecimal realizedIncome = BigDecimal.ZERO;
    private BigDecimal realizedExpenses = BigDecimal.ZERO;
    private BigDecimal realizedSavings = BigDecimal.ZERO;
    private BigDecimal recommendedDailyLimit = BigDecimal.ZERO;
    private BigDecimal recommendedWeeklyLimit = BigDecimal.ZERO;

    public BudgetDto() {}

    public BudgetDto(Budget budget) {
        if (budget != null) {
            this.id = budget.getId();
            this.month = budget.getMonth();
            this.year = budget.getYear();
            this.expectedIncome = budget.getExpectedIncome();
            this.expectedExtraIncome = budget.getExpectedExtraIncome();
            this.fixedExpensesBudget = budget.getFixedExpensesBudget();
            this.variableExpensesBudget = budget.getVariableExpensesBudget();
            this.savingsTarget = budget.getSavingsTarget();
            this.salaryExpectedDate = budget.getSalaryExpectedDate();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public BigDecimal getExpectedIncome() { return expectedIncome; }
    public void setExpectedIncome(BigDecimal expectedIncome) { this.expectedIncome = expectedIncome; }

    public BigDecimal getExpectedExtraIncome() { return expectedExtraIncome; }
    public void setExpectedExtraIncome(BigDecimal expectedExtraIncome) { this.expectedExtraIncome = expectedExtraIncome; }

    public BigDecimal getFixedExpensesBudget() { return fixedExpensesBudget; }
    public void setFixedExpensesBudget(BigDecimal fixedExpensesBudget) { this.fixedExpensesBudget = fixedExpensesBudget; }

    public BigDecimal getVariableExpensesBudget() { return variableExpensesBudget; }
    public void setVariableExpensesBudget(BigDecimal variableExpensesBudget) { this.variableExpensesBudget = variableExpensesBudget; }

    public BigDecimal getSavingsTarget() { return savingsTarget; }
    public void setSavingsTarget(BigDecimal savingsTarget) { this.savingsTarget = savingsTarget; }

    public LocalDate getSalaryExpectedDate() { return salaryExpectedDate; }
    public void setSalaryExpectedDate(LocalDate salaryExpectedDate) { this.salaryExpectedDate = salaryExpectedDate; }

    public BigDecimal getTotalExpectedIncome() { return totalExpectedIncome; }
    public void setTotalExpectedIncome(BigDecimal totalExpectedIncome) { this.totalExpectedIncome = totalExpectedIncome; }

    public BigDecimal getTotalBudgetedExpenses() { return totalBudgetedExpenses; }
    public void setTotalBudgetedExpenses(BigDecimal totalBudgetedExpenses) { this.totalBudgetedExpenses = totalBudgetedExpenses; }

    public BigDecimal getPlannedSavings() { return plannedSavings; }
    public void setPlannedSavings(BigDecimal plannedSavings) { this.plannedSavings = plannedSavings; }

    public BigDecimal getRealizedIncome() { return realizedIncome; }
    public void setRealizedIncome(BigDecimal realizedIncome) { this.realizedIncome = realizedIncome; }

    public BigDecimal getRealizedExpenses() { return realizedExpenses; }
    public void setRealizedExpenses(BigDecimal realizedExpenses) { this.realizedExpenses = realizedExpenses; }

    public BigDecimal getRealizedSavings() { return realizedSavings; }
    public void setRealizedSavings(BigDecimal realizedSavings) { this.realizedSavings = realizedSavings; }

    public BigDecimal getRecommendedDailyLimit() { return recommendedDailyLimit; }
    public void setRecommendedDailyLimit(BigDecimal recommendedDailyLimit) { this.recommendedDailyLimit = recommendedDailyLimit; }

    public BigDecimal getRecommendedWeeklyLimit() { return recommendedWeeklyLimit; }
    public void setRecommendedWeeklyLimit(BigDecimal recommendedWeeklyLimit) { this.recommendedWeeklyLimit = recommendedWeeklyLimit; }
}
