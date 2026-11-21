package com.metron.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WeeklyEvaluationDto {

    private LocalDate periodStart;
    private LocalDate periodEnd;

    private BigDecimal totalReceived = BigDecimal.ZERO;
    private BigDecimal totalSpent = BigDecimal.ZERO;
    private BigDecimal weekBalance = BigDecimal.ZERO;

    private String topSpendingCategory = "Nenhuma";
    private BigDecimal topSpendingCategoryAmount = BigDecimal.ZERO;

    private BigDecimal previousWeekSpent = BigDecimal.ZERO;
    private BigDecimal growthPercentageVsPreviousWeek = BigDecimal.ZERO;

    private BigDecimal budgetExcessAmount = BigDecimal.ZERO;
    private BigDecimal recurringExpensesTotal = BigDecimal.ZERO;
    private List<String> recurringExpensesList = new ArrayList<>();

    private BigDecimal actualSavings = BigDecimal.ZERO;
    private List<String> recommendations = new ArrayList<>();

    public WeeklyEvaluationDto() {}

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public BigDecimal getTotalReceived() { return totalReceived; }
    public void setTotalReceived(BigDecimal totalReceived) { this.totalReceived = totalReceived; }

    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }

    public BigDecimal getWeekBalance() { return weekBalance; }
    public void setWeekBalance(BigDecimal weekBalance) { this.weekBalance = weekBalance; }

    public String getTopSpendingCategory() { return topSpendingCategory; }
    public void setTopSpendingCategory(String topSpendingCategory) { this.topSpendingCategory = topSpendingCategory; }

    public BigDecimal getTopSpendingCategoryAmount() { return topSpendingCategoryAmount; }
    public void setTopSpendingCategoryAmount(BigDecimal topSpendingCategoryAmount) { this.topSpendingCategoryAmount = topSpendingCategoryAmount; }

    public BigDecimal getPreviousWeekSpent() { return previousWeekSpent; }
    public void setPreviousWeekSpent(BigDecimal previousWeekSpent) { this.previousWeekSpent = previousWeekSpent; }

    public BigDecimal getGrowthPercentageVsPreviousWeek() { return growthPercentageVsPreviousWeek; }
    public void setGrowthPercentageVsPreviousWeek(BigDecimal growthPercentageVsPreviousWeek) { this.growthPercentageVsPreviousWeek = growthPercentageVsPreviousWeek; }

    public BigDecimal getBudgetExcessAmount() { return budgetExcessAmount; }
    public void setBudgetExcessAmount(BigDecimal budgetExcessAmount) { this.budgetExcessAmount = budgetExcessAmount; }

    public BigDecimal getRecurringExpensesTotal() { return recurringExpensesTotal; }
    public void setRecurringExpensesTotal(BigDecimal recurringExpensesTotal) { this.recurringExpensesTotal = recurringExpensesTotal; }

    public List<String> getRecurringExpensesList() { return recurringExpensesList; }
    public void setRecurringExpensesList(List<String> recurringExpensesList) { this.recurringExpensesList = recurringExpensesList; }

    public BigDecimal getActualSavings() { return actualSavings; }
    public void setActualSavings(BigDecimal actualSavings) { this.actualSavings = actualSavings; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
}
