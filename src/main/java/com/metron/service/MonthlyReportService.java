package com.metron.service;

import com.metron.dto.MonthlyReportDto;
import com.metron.model.entity.*;
import com.metron.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MonthlyReportService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final BudgetRepository budgetRepository;
    private final FinancialGoalRepository goalRepository;

    public MonthlyReportService(ExpenseRepository expenseRepository,
                                IncomeRepository incomeRepository,
                                BudgetRepository budgetRepository,
                                FinancialGoalRepository goalRepository) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.budgetRepository = budgetRepository;
        this.goalRepository = goalRepository;
    }

    public MonthlyReportDto generateMonthlyReport(User user, Integer month, Integer year) {
        if (month == null || year == null) {
            LocalDate now = LocalDate.now();
            month = now.getMonthValue();
            year = now.getYear();
        }

        YearMonth ym = YearMonth.of(year, month);
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();

        MonthlyReportDto dto = new MonthlyReportDto();
        dto.setMonth(month);
        dto.setYear(year);

        // 1. Total income received
        BigDecimal totalIncome = incomeRepository.sumAmountByUserIdAndReceiptStatusAndReceiptDateBetween(
                user.getId(), ReceiptStatus.RECEBIDO, startOfMonth, endOfMonth);
        if (totalIncome == null || totalIncome.compareTo(BigDecimal.ZERO) == 0) {
            totalIncome = user.getMonthlyIncome() != null ? user.getMonthlyIncome() : BigDecimal.ZERO;
        }
        dto.setTotalIncome(totalIncome);

        // 2. Total paid expenses
        BigDecimal totalExpenses = expenseRepository.sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
                user.getId(), PaymentStatus.PAGO, startOfMonth, endOfMonth);
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;
        dto.setTotalExpenses(totalExpenses);

        // 3. Final balance & saved amount
        BigDecimal finalBalance = totalIncome.subtract(totalExpenses);
        dto.setFinalBalance(finalBalance);

        BigDecimal saved = finalBalance.max(BigDecimal.ZERO);
        dto.setSavedAmount(saved);

        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal savingsPct = saved.multiply(new BigDecimal("100")).divide(totalIncome, 1, RoundingMode.HALF_UP);
            dto.setSavingsRatePercent(savingsPct);
        } else {
            dto.setSavingsRatePercent(BigDecimal.ZERO);
        }

        // 4. Top Spending Category
        List<Object[]> categoryList = expenseRepository.findCategoryExpensesBetween(user.getId(), startOfMonth, endOfMonth);
        if (!categoryList.isEmpty()) {
            Object[] top = categoryList.get(0);
            dto.setTopSpendingCategory((String) top[0]);
            BigDecimal topAmt = (BigDecimal) top[1];
            dto.setTopSpendingCategoryAmount(topAmt);
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal topPct = topAmt.multiply(new BigDecimal("100")).divide(totalExpenses, 1, RoundingMode.HALF_UP);
                dto.setTopSpendingCategoryPercent(topPct);
            }
        }

        // 5. Comparison with previous month
        YearMonth prevYm = ym.minusMonths(1);
        BigDecimal prevMonthExpenses = expenseRepository.sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
                user.getId(), PaymentStatus.PAGO, prevYm.atDay(1), prevYm.atEndOfMonth());
        if (prevMonthExpenses == null) prevMonthExpenses = BigDecimal.ZERO;
        dto.setPreviousMonthTotalExpense(prevMonthExpenses);

        if (prevMonthExpenses.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = totalExpenses.subtract(prevMonthExpenses);
            BigDecimal diffPct = diff.multiply(new BigDecimal("100")).divide(prevMonthExpenses, 1, RoundingMode.HALF_UP);
            dto.setMonthOverMonthExpenseDiffPercent(diffPct);
        } else {
            dto.setMonthOverMonthExpenseDiffPercent(BigDecimal.ZERO);
        }

        // 6. Weekly Comparison within month
        LocalDate wStart = startOfMonth;
        int weekCount = 1;
        while (!wStart.isAfter(endOfMonth)) {
            LocalDate wEnd = wStart.plusDays(6).isAfter(endOfMonth) ? endOfMonth : wStart.plusDays(6);
            BigDecimal wExp = expenseRepository.sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
                    user.getId(), PaymentStatus.PAGO, wStart, wEnd);
            dto.getWeeklyComparison().add(new MonthlyReportDto.WeekSummary(
                    "Semana " + weekCount + " (" + wStart.getDayOfMonth() + "-" + wEnd.getDayOfMonth() + ")",
                    wExp != null ? wExp : BigDecimal.ZERO
            ));
            wStart = wEnd.plusDays(1);
            weekCount++;
        }

        // 7. Recurring expenses
        List<Expense> recurring = expenseRepository.findRecurringExpenses(user.getId());
        List<String> recDesc = recurring.stream()
                .map(e -> String.format("%s - R$ %s", e.getDescription(), e.getAmount().setScale(2, RoundingMode.HALF_UP)))
                .collect(Collectors.toList());
        dto.setRecurringExpenses(recDesc);

        // 8. Budget excess analysis
        final BigDecimal finalTotalExpenses = totalExpenses;
        budgetRepository.findByUserIdAndMonthAndYear(user.getId(), month, year).ifPresent(b -> {
            BigDecimal budgetedTotal = b.getFixedExpensesBudget().add(b.getVariableExpensesBudget());
            if (finalTotalExpenses.compareTo(budgetedTotal) > 0) {
                BigDecimal excess = finalTotalExpenses.subtract(budgetedTotal);
                dto.getBudgetExcessList().add(new MonthlyReportDto.BudgetExcessCategory(
                        "Orçamento Global do Mês", budgetedTotal, finalTotalExpenses, excess
                ));
            }
        });

        // 9. Goals achieved and pending
        List<FinancialGoal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        long achieved = goals.stream().filter(g -> g.getStatus() == GoalStatus.CONCLUIDA).count();
        long pending = goals.stream().filter(g -> g.getStatus() == GoalStatus.EM_ANDAMENTO).count();
        dto.setAchievedGoalsCount(achieved);
        dto.setPendingGoalsCount(pending);

        // 10. Recommendations for next month
        if (dto.getSavingsRatePercent().compareTo(new BigDecimal("20")) >= 0) {
            dto.getRecommendationsForNextMonth().add(String.format(
                    "Parabéns! Sua taxa de poupança atingiu %.1f%% da sua renda. Mantenha essa consistência.",
                    dto.getSavingsRatePercent().doubleValue()
            ));
        } else if (dto.getSavingsRatePercent().compareTo(new BigDecimal("10")) < 0) {
            dto.getRecommendationsForNextMonth().add(
                    "Sua poupança deste mês ficou abaixo de 10% da renda. Estabeleça uma meta de guardar no mínimo 10% logo no dia do recebimento do próximo salário."
            );
        }

        if (dto.getMonthOverMonthExpenseDiffPercent().compareTo(new BigDecimal("10")) > 0) {
            dto.getRecommendationsForNextMonth().add(String.format(
                    "Seus gastos gerais cresceram %.1f%% em comparação ao mês anterior. Avalie os custos da categoria '%s'.",
                    dto.getMonthOverMonthExpenseDiffPercent().doubleValue(), dto.getTopSpendingCategory()
            ));
        }

        if (!recurring.isEmpty()) {
            dto.getRecommendationsForNextMonth().add(
                    "Você possui " + recurring.size() + " despesas recorrentes cadastradas. Revise mensalmente assinaturas que você não utiliza mais."
            );
        }

        if (dto.getRecommendationsForNextMonth().isEmpty()) {
            dto.getRecommendationsForNextMonth().add(
                    "Continue mantendo seu planejamento equilibrado e acompanhe seu limite diário na METRON no próximo mês."
            );
        }

        return dto;
    }
}
