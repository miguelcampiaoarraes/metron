package com.metron.dto;

import com.metron.model.entity.FinancialGoal;
import com.metron.model.entity.GoalStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FinancialGoalDto {

    private Long id;

    @NotBlank(message = "O título do objetivo é obrigatório.")
    private String title;

    @NotNull(message = "O valor desejado é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor desejado deve ser positivo.")
    private BigDecimal targetAmount;

    private BigDecimal currentAmount = BigDecimal.ZERO;
    private LocalDate deadline;
    private String category;
    private GoalStatus status = GoalStatus.EM_ANDAMENTO;
    private String notes;
    private LocalDateTime createdAt;

    // Computed
    private BigDecimal progressPercentage = BigDecimal.ZERO;
    private BigDecimal remainingAmount = BigDecimal.ZERO;
    private BigDecimal monthlySavingsNeeded = BigDecimal.ZERO;

    public FinancialGoalDto() {}

    public FinancialGoalDto(FinancialGoal goal) {
        if (goal != null) {
            this.id = goal.getId();
            this.title = goal.getTitle();
            this.targetAmount = goal.getTargetAmount();
            this.currentAmount = goal.getCurrentAmount();
            this.deadline = goal.getDeadline();
            this.category = goal.getCategory();
            this.status = goal.getStatus();
            this.notes = goal.getNotes();
            this.createdAt = goal.getCreatedAt();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

    public BigDecimal getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(BigDecimal currentAmount) { this.currentAmount = currentAmount; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public GoalStatus getStatus() { return status; }
    public void setStatus(GoalStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public BigDecimal getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(BigDecimal progressPercentage) { this.progressPercentage = progressPercentage; }

    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }

    public BigDecimal getMonthlySavingsNeeded() { return monthlySavingsNeeded; }
    public void setMonthlySavingsNeeded(BigDecimal monthlySavingsNeeded) { this.monthlySavingsNeeded = monthlySavingsNeeded; }
}
