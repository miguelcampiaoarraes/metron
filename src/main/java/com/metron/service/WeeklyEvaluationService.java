package com.metron.service;

import com.metron.dto.WeeklyEvaluationDto;
import com.metron.model.entity.Expense;
import com.metron.model.entity.PaymentStatus;
import com.metron.model.entity.ReceiptStatus;
import com.metron.model.entity.User;
import com.metron.repository.ExpenseRepository;
import com.metron.repository.IncomeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WeeklyEvaluationService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

    public WeeklyEvaluationService(ExpenseRepository expenseRepository,
                                   IncomeRepository incomeRepository) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
    }

    public WeeklyEvaluationDto evaluateCurrentWeek(User user) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        LocalDate startOfPrevWeek = startOfWeek.minusWeeks(1);
        LocalDate endOfPrevWeek = startOfWeek.minusDays(1);

        WeeklyEvaluationDto dto = new WeeklyEvaluationDto();
        dto.setPeriodStart(startOfWeek);
        dto.setPeriodEnd(endOfWeek);

        // Current week totals
        BigDecimal totalReceived = incomeRepository.sumAmountByUserIdAndReceiptStatusAndReceiptDateBetween(
                user.getId(), ReceiptStatus.RECEBIDO, startOfWeek, endOfWeek);
        if (totalReceived == null) totalReceived = BigDecimal.ZERO;
        dto.setTotalReceived(totalReceived);

        BigDecimal totalSpent = expenseRepository.sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
                user.getId(), PaymentStatus.PAGO, startOfWeek, endOfWeek);
        if (totalSpent == null) totalSpent = BigDecimal.ZERO;
        dto.setTotalSpent(totalSpent);

        dto.setWeekBalance(totalReceived.subtract(totalSpent));
        dto.setActualSavings(dto.getWeekBalance().max(BigDecimal.ZERO));

        // Previous week total
        BigDecimal prevSpent = expenseRepository.sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
                user.getId(), PaymentStatus.PAGO, startOfPrevWeek, endOfPrevWeek);
        if (prevSpent == null) prevSpent = BigDecimal.ZERO;
        dto.setPreviousWeekSpent(prevSpent);

        if (prevSpent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = totalSpent.subtract(prevSpent);
            BigDecimal growthPct = diff.multiply(new BigDecimal("100")).divide(prevSpent, 1, RoundingMode.HALF_UP);
            dto.setGrowthPercentageVsPreviousWeek(growthPct);
        } else {
            dto.setGrowthPercentageVsPreviousWeek(BigDecimal.ZERO);
        }

        // Top Category
        List<Object[]> topCategories = expenseRepository.findCategoryExpensesBetween(user.getId(), startOfWeek, endOfWeek);
        if (!topCategories.isEmpty()) {
            Object[] top = topCategories.get(0);
            dto.setTopSpendingCategory((String) top[0]);
            dto.setTopSpendingCategoryAmount((BigDecimal) top[1]);
        }

        // Recurring expenses in this week
        List<Expense> recurring = expenseRepository.findRecurringExpenses(user.getId());
        List<String> recDesc = recurring.stream()
                .map(e -> String.format("%s (R$ %s)", e.getDescription(), e.getAmount().setScale(2, RoundingMode.HALF_UP)))
                .collect(Collectors.toList());
        dto.setRecurringExpensesList(recDesc);

        BigDecimal recTotal = recurring.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setRecurringExpensesTotal(recTotal);

        // Actionable Recommendations based on actual data
        if (dto.getGrowthPercentageVsPreviousWeek().compareTo(new BigDecimal("15")) > 0) {
            dto.getRecommendations().add(String.format(
                    "Você gastou %.1f%% a mais em relação à semana anterior. Cuidado para não acelerar despesas discricionárias.",
                    dto.getGrowthPercentageVsPreviousWeek().doubleValue()
            ));
        }

        if (dto.getTopSpendingCategoryAmount().compareTo(BigDecimal.ZERO) > 0 && totalSpent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal catPct = dto.getTopSpendingCategoryAmount().multiply(new BigDecimal("100")).divide(totalSpent, 1, RoundingMode.HALF_UP);
            if (catPct.compareTo(new BigDecimal("40")) > 0) {
                dto.getRecommendations().add(String.format(
                        "A categoria '%s' representou %.1f%% dos seus gastos semanais (R$ %s). Avalie oportunidades de otimização nessa área.",
                        dto.getTopSpendingCategory(), catPct.doubleValue(), dto.getTopSpendingCategoryAmount().setScale(2, RoundingMode.HALF_UP)
                ));
            }
        }

        if (totalSpent.compareTo(totalReceived) > 0 && totalReceived.compareTo(BigDecimal.ZERO) > 0) {
            dto.getRecommendations().add("Nesta semana as saídas superaram as entradas recebidas. Reduza despesas extras na próxima semana para reequilibrar o fluxo.");
        } else if (dto.getWeekBalance().compareTo(BigDecimal.ZERO) > 0) {
            dto.getRecommendations().add(String.format(
                    "Excelente disciplina financeira! Você fechou a semana com saldo positivo de R$ %s. Considere transferir parte desse valor para sua reserva.",
                    dto.getWeekBalance().setScale(2, RoundingMode.HALF_UP)
            ));
        }

        if (dto.getRecommendations().isEmpty()) {
            dto.getRecommendations().add("Seus hábitos financeiros desta semana estão consistentes com seu planejamento. Mantenha os registros atualizados diariamente.");
        }

        return dto;
    }
}
