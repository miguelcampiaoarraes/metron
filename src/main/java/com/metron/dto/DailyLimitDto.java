package com.metron.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyLimitDto {

    private BigDecimal planningAvailableBalance = BigDecimal.ZERO;
    private BigDecimal recommendedDailyLimit = BigDecimal.ZERO;
    private BigDecimal recommendedWeeklyLimit = BigDecimal.ZERO;
    private BigDecimal spentToday = BigDecimal.ZERO;
    private BigDecimal remainingToday = BigDecimal.ZERO;
    private long daysUntilNextSalary = 1;
    private LocalDate nextSalaryDate;

    // Status: VERDE (OK), AMARELO (Warning/near limit), VERMELHO (Exceeded)
    private String budgetHealthStatus = "VERDE";
    private String healthMessage;
    private boolean hasSufficientData = true;
    private String explanationMessage;

    public DailyLimitDto() {}

    public BigDecimal getPlanningAvailableBalance() { return planningAvailableBalance; }
    public void setPlanningAvailableBalance(BigDecimal planningAvailableBalance) { this.planningAvailableBalance = planningAvailableBalance; }

    public BigDecimal getRecommendedDailyLimit() { return recommendedDailyLimit; }
    public void setRecommendedDailyLimit(BigDecimal recommendedDailyLimit) { this.recommendedDailyLimit = recommendedDailyLimit; }

    public BigDecimal getRecommendedWeeklyLimit() { return recommendedWeeklyLimit; }
    public void setRecommendedWeeklyLimit(BigDecimal recommendedWeeklyLimit) { this.recommendedWeeklyLimit = recommendedWeeklyLimit; }

    public BigDecimal getSpentToday() { return spentToday; }
    public void setSpentToday(BigDecimal spentToday) { this.spentToday = spentToday; }

    public BigDecimal getRemainingToday() { return remainingToday; }
    public void setRemainingToday(BigDecimal remainingToday) { this.remainingToday = remainingToday; }

    public long getDaysUntilNextSalary() { return daysUntilNextSalary; }
    public void setDaysUntilNextSalary(long daysUntilNextSalary) { this.daysUntilNextSalary = daysUntilNextSalary; }

    public LocalDate getNextSalaryDate() { return nextSalaryDate; }
    public void setNextSalaryDate(LocalDate nextSalaryDate) { this.nextSalaryDate = nextSalaryDate; }

    public String getBudgetHealthStatus() { return budgetHealthStatus; }
    public void setBudgetHealthStatus(String budgetHealthStatus) { this.budgetHealthStatus = budgetHealthStatus; }

    public String getHealthMessage() { return healthMessage; }
    public void setHealthMessage(String healthMessage) { this.healthMessage = healthMessage; }

    public boolean isHasSufficientData() { return hasSufficientData; }
    public void setHasSufficientData(boolean hasSufficientData) { this.hasSufficientData = hasSufficientData; }

    public String getExplanationMessage() { return explanationMessage; }
    public void setExplanationMessage(String explanationMessage) { this.explanationMessage = explanationMessage; }
}
