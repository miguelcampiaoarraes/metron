package com.metron.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MonthlyReportDto {

    private Integer month;
    private Integer year;

    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private BigDecimal finalBalance = BigDecimal.ZERO;
    private BigDecimal savedAmount = BigDecimal.ZERO;
    private BigDecimal savingsRatePercent = BigDecimal.ZERO;

    private String topSpendingCategory = "Nenhuma";
    private BigDecimal topSpendingCategoryAmount = BigDecimal.ZERO;
    private BigDecimal topSpendingCategoryPercent = BigDecimal.ZERO;

    private BigDecimal previousMonthTotalExpense = BigDecimal.ZERO;
    private BigDecimal monthOverMonthExpenseDiffPercent = BigDecimal.ZERO;

    private List<WeekSummary> weeklyComparison = new ArrayList<>();
    private List<String> recurringExpenses = new ArrayList<>();
    private List<BudgetExcessCategory> budgetExcessList = new ArrayList<>();

    private long achievedGoalsCount = 0;
    private long pendingGoalsCount = 0;

    private List<String> recommendationsForNextMonth = new ArrayList<>();

    public MonthlyReportDto() {}

    public static class WeekSummary {
        private String weekLabel;
        private BigDecimal totalExpense;

        public WeekSummary() {}
        public WeekSummary(String weekLabel, BigDecimal totalExpense) {
            this.weekLabel = weekLabel;
            this.totalExpense = totalExpense;
        }

        public String getWeekLabel() { return weekLabel; }
        public void setWeekLabel(String weekLabel) { this.weekLabel = weekLabel; }
        public BigDecimal getTotalExpense() { return totalExpense; }
        public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }
    }

    public static class BudgetExcessCategory {
        private String category;
        private BigDecimal budgeted;
        private BigDecimal actual;
        private BigDecimal excess;

        public BudgetExcessCategory() {}
        public BudgetExcessCategory(String category, BigDecimal budgeted, BigDecimal actual, BigDecimal excess) {
            this.category = category;
            this.budgeted = budgeted;
            this.actual = actual;
            this.excess = excess;
        }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public BigDecimal getBudgeted() { return budgeted; }
        public void setBudgeted(BigDecimal budgeted) { this.budgeted = budgeted; }
        public BigDecimal getActual() { return actual; }
        public void setActual(BigDecimal actual) { this.actual = actual; }
        public BigDecimal getExcess() { return excess; }
        public void setExcess(BigDecimal excess) { this.excess = excess; }
    }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

    public BigDecimal getFinalBalance() { return finalBalance; }
    public void setFinalBalance(BigDecimal finalBalance) { this.finalBalance = finalBalance; }

    public BigDecimal getSavedAmount() { return savedAmount; }
    public void setSavedAmount(BigDecimal savedAmount) { this.savedAmount = savedAmount; }

    public BigDecimal getSavingsRatePercent() { return savingsRatePercent; }
    public void setSavingsRatePercent(BigDecimal savingsRatePercent) { this.savingsRatePercent = savingsRatePercent; }

    public String getTopSpendingCategory() { return topSpendingCategory; }
    public void setTopSpendingCategory(String topSpendingCategory) { this.topSpendingCategory = topSpendingCategory; }

    public BigDecimal getTopSpendingCategoryAmount() { return topSpendingCategoryAmount; }
    public void setTopSpendingCategoryAmount(BigDecimal topSpendingCategoryAmount) { this.topSpendingCategoryAmount = topSpendingCategoryAmount; }

    public BigDecimal getTopSpendingCategoryPercent() { return topSpendingCategoryPercent; }
    public void setTopSpendingCategoryPercent(BigDecimal topSpendingCategoryPercent) { this.topSpendingCategoryPercent = topSpendingCategoryPercent; }

    public BigDecimal getPreviousMonthTotalExpense() { return previousMonthTotalExpense; }
    public void setPreviousMonthTotalExpense(BigDecimal previousMonthTotalExpense) { this.previousMonthTotalExpense = previousMonthTotalExpense; }

    public BigDecimal getMonthOverMonthExpenseDiffPercent() { return monthOverMonthExpenseDiffPercent; }
    public void setMonthOverMonthExpenseDiffPercent(BigDecimal monthOverMonthExpenseDiffPercent) { this.monthOverMonthExpenseDiffPercent = monthOverMonthExpenseDiffPercent; }

    public List<WeekSummary> getWeeklyComparison() { return weeklyComparison; }
    public void setWeeklyComparison(List<WeekSummary> weeklyComparison) { this.weeklyComparison = weeklyComparison; }

    public List<String> getRecurringExpenses() { return recurringExpenses; }
    public void setRecurringExpenses(List<String> recurringExpenses) { this.recurringExpenses = recurringExpenses; }

    public List<BudgetExcessCategory> getBudgetExcessList() { return budgetExcessList; }
    public void setBudgetExcessList(List<BudgetExcessCategory> budgetExcessList) { this.budgetExcessList = budgetExcessList; }

    public long getAchievedGoalsCount() { return achievedGoalsCount; }
    public void setAchievedGoalsCount(long achievedGoalsCount) { this.achievedGoalsCount = achievedGoalsCount; }

    public long getPendingGoalsCount() { return pendingGoalsCount; }
    public void setPendingGoalsCount(long pendingGoalsCount) { this.pendingGoalsCount = pendingGoalsCount; }

    public List<String> getRecommendationsForNextMonth() { return recommendationsForNextMonth; }
    public void setRecommendationsForNextMonth(List<String> recommendationsForNextMonth) { this.recommendationsForNextMonth = recommendationsForNextMonth; }
}
