package com.metron.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardSummaryDto {

    // Main 8 Cards
    private BigDecimal monthlyIncome = BigDecimal.ZERO;
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private BigDecimal userBankBalance = BigDecimal.ZERO;
    private BigDecimal planningAvailableBalance = BigDecimal.ZERO;
    private BigDecimal reservedForSavings = BigDecimal.ZERO;
    private BigDecimal incomeUsedPercentage = BigDecimal.ZERO;
    private BigDecimal monthlySavingsGoal = BigDecimal.ZERO;
    private BigDecimal pendingBillsTotal = BigDecimal.ZERO;

    // Daily Limit snapshot
    private DailyLimitDto dailyLimit;

    // Charts data structures
    private List<CategoryExpenseData> categoryExpenses = new ArrayList<>();
    private List<DailyExpenseData> dailyEvolution = new ArrayList<>();
    private List<WeeklyExpenseData> weeklyComparison = new ArrayList<>();
    private IncomeVsExpenseData incomeVsExpense = new IncomeVsExpenseData();
    private PlannedVsRealizedData plannedVsRealized = new PlannedVsRealizedData();
    private List<MonthlySavingsData> savingsEvolution = new ArrayList<>();

    public DashboardSummaryDto() {}

    public static class CategoryExpenseData {
        private String category;
        private BigDecimal amount;
        private BigDecimal percentage;
        private String color;

        public CategoryExpenseData() {}
        public CategoryExpenseData(String category, BigDecimal amount, BigDecimal percentage, String color) {
            this.category = category;
            this.amount = amount;
            this.percentage = percentage;
            this.color = color;
        }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public BigDecimal getPercentage() { return percentage; }
        public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    public static class DailyExpenseData {
        private String date;
        private BigDecimal amount;

        public DailyExpenseData() {}
        public DailyExpenseData(String date, BigDecimal amount) {
            this.date = date;
            this.amount = amount;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    public static class WeeklyExpenseData {
        private String weekLabel;
        private BigDecimal amount;

        public WeeklyExpenseData() {}
        public WeeklyExpenseData(String weekLabel, BigDecimal amount) {
            this.weekLabel = weekLabel;
            this.amount = amount;
        }

        public String getWeekLabel() { return weekLabel; }
        public void setWeekLabel(String weekLabel) { this.weekLabel = weekLabel; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    public static class IncomeVsExpenseData {
        private BigDecimal totalIncome = BigDecimal.ZERO;
        private BigDecimal totalExpense = BigDecimal.ZERO;
        private BigDecimal netBalance = BigDecimal.ZERO;

        public IncomeVsExpenseData() {}
        public IncomeVsExpenseData(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal netBalance) {
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.netBalance = netBalance;
        }

        public BigDecimal getTotalIncome() { return totalIncome; }
        public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
        public BigDecimal getTotalExpense() { return totalExpense; }
        public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }
        public BigDecimal getNetBalance() { return netBalance; }
        public void setNetBalance(BigDecimal netBalance) { this.netBalance = netBalance; }
    }

    public static class PlannedVsRealizedData {
        private BigDecimal plannedIncome = BigDecimal.ZERO;
        private BigDecimal realizedIncome = BigDecimal.ZERO;
        private BigDecimal plannedExpense = BigDecimal.ZERO;
        private BigDecimal realizedExpense = BigDecimal.ZERO;

        public PlannedVsRealizedData() {}
        public PlannedVsRealizedData(BigDecimal plannedIncome, BigDecimal realizedIncome, BigDecimal plannedExpense, BigDecimal realizedExpense) {
            this.plannedIncome = plannedIncome;
            this.realizedIncome = realizedIncome;
            this.plannedExpense = plannedExpense;
            this.realizedExpense = realizedExpense;
        }

        public BigDecimal getPlannedIncome() { return plannedIncome; }
        public void setPlannedIncome(BigDecimal plannedIncome) { this.plannedIncome = plannedIncome; }
        public BigDecimal getRealizedIncome() { return realizedIncome; }
        public void setRealizedIncome(BigDecimal realizedIncome) { this.realizedIncome = realizedIncome; }
        public BigDecimal getPlannedExpense() { return plannedExpense; }
        public void setPlannedExpense(BigDecimal plannedExpense) { this.plannedExpense = plannedExpense; }
        public BigDecimal getRealizedExpense() { return realizedExpense; }
        public void setRealizedExpense(BigDecimal realizedExpense) { this.realizedExpense = realizedExpense; }
    }

    public static class MonthlySavingsData {
        private String monthLabel;
        private BigDecimal savedAmount;

        public MonthlySavingsData() {}
        public MonthlySavingsData(String monthLabel, BigDecimal savedAmount) {
            this.monthLabel = monthLabel;
            this.savedAmount = savedAmount;
        }

        public String getMonthLabel() { return monthLabel; }
        public void setMonthLabel(String monthLabel) { this.monthLabel = monthLabel; }
        public BigDecimal getSavedAmount() { return savedAmount; }
        public void setSavedAmount(BigDecimal savedAmount) { this.savedAmount = savedAmount; }
    }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

    public BigDecimal getUserBankBalance() { return userBankBalance; }
    public void setUserBankBalance(BigDecimal userBankBalance) { this.userBankBalance = userBankBalance; }

    public BigDecimal getPlanningAvailableBalance() { return planningAvailableBalance; }
    public void setPlanningAvailableBalance(BigDecimal planningAvailableBalance) { this.planningAvailableBalance = planningAvailableBalance; }

    public BigDecimal getReservedForSavings() { return reservedForSavings; }
    public void setReservedForSavings(BigDecimal reservedForSavings) { this.reservedForSavings = reservedForSavings; }

    public BigDecimal getIncomeUsedPercentage() { return incomeUsedPercentage; }
    public void setIncomeUsedPercentage(BigDecimal incomeUsedPercentage) { this.incomeUsedPercentage = incomeUsedPercentage; }

    public BigDecimal getMonthlySavingsGoal() { return monthlySavingsGoal; }
    public void setMonthlySavingsGoal(BigDecimal monthlySavingsGoal) { this.monthlySavingsGoal = monthlySavingsGoal; }

    public BigDecimal getPendingBillsTotal() { return pendingBillsTotal; }
    public void setPendingBillsTotal(BigDecimal pendingBillsTotal) { this.pendingBillsTotal = pendingBillsTotal; }

    public DailyLimitDto getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(DailyLimitDto dailyLimit) { this.dailyLimit = dailyLimit; }

    public List<CategoryExpenseData> getCategoryExpenses() { return categoryExpenses; }
    public void setCategoryExpenses(List<CategoryExpenseData> categoryExpenses) { this.categoryExpenses = categoryExpenses; }

    public List<DailyExpenseData> getDailyEvolution() { return dailyEvolution; }
    public void setDailyEvolution(List<DailyExpenseData> dailyEvolution) { this.dailyEvolution = dailyEvolution; }

    public List<WeeklyExpenseData> getWeeklyComparison() { return weeklyComparison; }
    public void setWeeklyComparison(List<WeeklyExpenseData> weeklyComparison) { this.weeklyComparison = weeklyComparison; }

    public IncomeVsExpenseData getIncomeVsExpense() { return incomeVsExpense; }
    public void setIncomeVsExpense(IncomeVsExpenseData incomeVsExpense) { this.incomeVsExpense = incomeVsExpense; }

    public PlannedVsRealizedData getPlannedVsRealized() { return plannedVsRealized; }
    public void setPlannedVsRealized(PlannedVsRealizedData plannedVsRealized) { this.plannedVsRealized = plannedVsRealized; }

    public List<MonthlySavingsData> getSavingsEvolution() { return savingsEvolution; }
    public void setSavingsEvolution(List<MonthlySavingsData> savingsEvolution) { this.savingsEvolution = savingsEvolution; }
}
