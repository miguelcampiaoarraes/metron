package com.metron.service;

import com.metron.dto.BudgetDto;
import com.metron.model.entity.Budget;
import com.metron.model.entity.PaymentStatus;
import com.metron.model.entity.ReceiptStatus;
import com.metron.model.entity.User;
import com.metron.repository.BudgetRepository;
import com.metron.repository.ExpenseRepository;
import com.metron.repository.IncomeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         IncomeRepository incomeRepository,
                         ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
    }

    public BudgetDto getBudget(User user, Integer month, Integer year) {
        if (month == null || year == null) {
            LocalDate now = LocalDate.now();
            month = now.getMonthValue();
            year = now.getYear();
        }

        final int targetMonth = month;
        final int targetYear = year;

        Budget budget = budgetRepository.findByUserIdAndMonthAndYear(user.getId(), targetMonth, targetYear)
                .orElseGet(() -> {
                    Budget b = new Budget();
                    b.setUser(user);
                    b.setMonth(targetMonth);
                    b.setYear(targetYear);
                    b.setExpectedIncome(user.getMonthlyIncome() != null ? user.getMonthlyIncome() : BigDecimal.ZERO);
                    b.setSavingsTarget(user.getSavingsGoalMonthly() != null ? user.getSavingsGoalMonthly() : BigDecimal.ZERO);

                    // Default sensible distribution (50% fixed, 30% variable, 20% savings)
                    BigDecimal income = b.getExpectedIncome();
                    if (income.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal fixed = income.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
                        BigDecimal variable = income.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
                        b.setFixedExpensesBudget(fixed);
                        b.setVariableExpensesBudget(variable);
                    } else {
                        b.setFixedExpensesBudget(BigDecimal.ZERO);
                        b.setVariableExpensesBudget(BigDecimal.ZERO);
                    }

                    int safeDay = Math.min(user.getSalaryDay() != null ? user.getSalaryDay() : 5, 28);
                    b.setSalaryExpectedDate(LocalDate.of(targetYear, targetMonth, safeDay));
                    return b;
                });

        BudgetDto dto = new BudgetDto(budget);

        // Realized data calculation
        YearMonth ym = YearMonth.of(targetYear, targetMonth);
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();

        BigDecimal realizedIncome = incomeRepository.sumAmountByUserIdAndReceiptStatusAndReceiptDateBetween(
                user.getId(), ReceiptStatus.RECEBIDO, startOfMonth, endOfMonth);
        BigDecimal realizedExpenses = expenseRepository.sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
                user.getId(), PaymentStatus.PAGO, startOfMonth, endOfMonth);

        dto.setRealizedIncome(realizedIncome != null ? realizedIncome : BigDecimal.ZERO);
        dto.setRealizedExpenses(realizedExpenses != null ? realizedExpenses : BigDecimal.ZERO);
        dto.setRealizedSavings(dto.getRealizedIncome().subtract(dto.getRealizedExpenses()));

        // Computed metrics
        BigDecimal totalExpected = budget.getExpectedIncome().add(budget.getExpectedExtraIncome());
        BigDecimal totalBudgeted = budget.getFixedExpensesBudget().add(budget.getVariableExpensesBudget());
        dto.setTotalExpectedIncome(totalExpected);
        dto.setTotalBudgetedExpenses(totalBudgeted);
        dto.setPlannedSavings(totalExpected.subtract(totalBudgeted));

        // Daily / Weekly limit within this month
        int daysInMonth = ym.lengthOfMonth();
        BigDecimal availableToSpend = totalExpected.subtract(budget.getFixedExpensesBudget()).subtract(budget.getSavingsTarget());
        if (availableToSpend.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal daily = availableToSpend.divide(BigDecimal.valueOf(daysInMonth), 2, RoundingMode.HALF_UP);
            dto.setRecommendedDailyLimit(daily);
            dto.setRecommendedWeeklyLimit(daily.multiply(BigDecimal.valueOf(7)));
        } else {
            dto.setRecommendedDailyLimit(BigDecimal.ZERO);
            dto.setRecommendedWeeklyLimit(BigDecimal.ZERO);
        }

        return dto;
    }

    @Transactional
    public BudgetDto saveOrUpdate(User user, BudgetDto dto) {
        Budget budget = budgetRepository.findByUserIdAndMonthAndYear(user.getId(), dto.getMonth(), dto.getYear())
                .orElse(new Budget());

        budget.setUser(user);
        budget.setMonth(dto.getMonth());
        budget.setYear(dto.getYear());
        budget.setExpectedIncome(dto.getExpectedIncome() != null ? dto.getExpectedIncome() : BigDecimal.ZERO);
        budget.setExpectedExtraIncome(dto.getExpectedExtraIncome() != null ? dto.getExpectedExtraIncome() : BigDecimal.ZERO);
        budget.setFixedExpensesBudget(dto.getFixedExpensesBudget() != null ? dto.getFixedExpensesBudget() : BigDecimal.ZERO);
        budget.setVariableExpensesBudget(dto.getVariableExpensesBudget() != null ? dto.getVariableExpensesBudget() : BigDecimal.ZERO);
        budget.setSavingsTarget(dto.getSavingsTarget() != null ? dto.getSavingsTarget() : BigDecimal.ZERO);
        budget.setSalaryExpectedDate(dto.getSalaryExpectedDate());

        Budget saved = budgetRepository.save(budget);
        return getBudget(user, saved.getMonth(), saved.getYear());
    }
}
