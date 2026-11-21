package com.metron.dto;

import java.math.BigDecimal;

public class PurchaseSimulationResponse {

    private boolean affordable;
    // Verdict: APROVADO, ATENCAO, DESACONSELHADO
    private String verdict;
    private String summaryMessage;
    private String detailedExplanation;

    private BigDecimal purchaseAmount;
    private BigDecimal currentDailyLimit;
    private BigDecimal remainingDailyAfterPurchase;
    private BigDecimal excessOverDailyLimit;

    private BigDecimal currentWeeklyBudget;
    private BigDecimal remainingWeeklyBudgetAfterPurchase;

    private BigDecimal currentMonthlyBudget;
    private BigDecimal remainingMonthlyBudgetAfterPurchase;

    private BigDecimal potentialSavingsReduction;
    private boolean savingsCompromised;
    private boolean pendingBillsAtRisk;

    public PurchaseSimulationResponse() {}

    public boolean isAffordable() { return affordable; }
    public void setAffordable(boolean affordable) { this.affordable = affordable; }

    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }

    public String getSummaryMessage() { return summaryMessage; }
    public void setSummaryMessage(String summaryMessage) { this.summaryMessage = summaryMessage; }

    public String getDetailedExplanation() { return detailedExplanation; }
    public void setDetailedExplanation(String detailedExplanation) { this.detailedExplanation = detailedExplanation; }

    public BigDecimal getPurchaseAmount() { return purchaseAmount; }
    public void setPurchaseAmount(BigDecimal purchaseAmount) { this.purchaseAmount = purchaseAmount; }

    public BigDecimal getCurrentDailyLimit() { return currentDailyLimit; }
    public void setCurrentDailyLimit(BigDecimal currentDailyLimit) { this.currentDailyLimit = currentDailyLimit; }

    public BigDecimal getRemainingDailyAfterPurchase() { return remainingDailyAfterPurchase; }
    public void setRemainingDailyAfterPurchase(BigDecimal remainingDailyAfterPurchase) { this.remainingDailyAfterPurchase = remainingDailyAfterPurchase; }

    public BigDecimal getExcessOverDailyLimit() { return excessOverDailyLimit; }
    public void setExcessOverDailyLimit(BigDecimal excessOverDailyLimit) { this.excessOverDailyLimit = excessOverDailyLimit; }

    public BigDecimal getCurrentWeeklyBudget() { return currentWeeklyBudget; }
    public void setCurrentWeeklyBudget(BigDecimal currentWeeklyBudget) { this.currentWeeklyBudget = currentWeeklyBudget; }

    public BigDecimal getRemainingWeeklyBudgetAfterPurchase() { return remainingWeeklyBudgetAfterPurchase; }
    public void setRemainingWeeklyBudgetAfterPurchase(BigDecimal remainingWeeklyBudgetAfterPurchase) { this.remainingWeeklyBudgetAfterPurchase = remainingWeeklyBudgetAfterPurchase; }

    public BigDecimal getCurrentMonthlyBudget() { return currentMonthlyBudget; }
    public void setCurrentMonthlyBudget(BigDecimal currentMonthlyBudget) { this.currentMonthlyBudget = currentMonthlyBudget; }

    public BigDecimal getRemainingMonthlyBudgetAfterPurchase() { return remainingMonthlyBudgetAfterPurchase; }
    public void setRemainingMonthlyBudgetAfterPurchase(BigDecimal remainingMonthlyBudgetAfterPurchase) { this.remainingMonthlyBudgetAfterPurchase = remainingMonthlyBudgetAfterPurchase; }

    public BigDecimal getPotentialSavingsReduction() { return potentialSavingsReduction; }
    public void setPotentialSavingsReduction(BigDecimal potentialSavingsReduction) { this.potentialSavingsReduction = potentialSavingsReduction; }

    public boolean isSavingsCompromised() { return savingsCompromised; }
    public void setSavingsCompromised(boolean savingsCompromised) { this.savingsCompromised = savingsCompromised; }

    public boolean isPendingBillsAtRisk() { return pendingBillsAtRisk; }
    public void setPendingBillsAtRisk(boolean pendingBillsAtRisk) { this.pendingBillsAtRisk = pendingBillsAtRisk; }
}
