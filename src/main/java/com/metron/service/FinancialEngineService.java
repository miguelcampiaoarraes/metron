package com.metron.service;

import com.metron.dto.DailyLimitDto;
import com.metron.dto.DashboardSummaryDto;
import com.metron.model.entity.PaymentStatus;
import com.metron.model.entity.ReceiptStatus;
import com.metron.model.entity.User;
import com.metron.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinancialEngineService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final BudgetRepository budgetRepository;
    private final FinancialGoalRepository goalRepository;

    public FinancialEngineService(ExpenseRepository expenseRepository,
                                  IncomeRepository incomeRepository,
                                  BudgetRepository budgetRepository,
                                  FinancialGoalRepository goalRepository) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.budgetRepository = budgetRepository;
        this.goalRepository = goalRepository;
    }

    /**
     * Core Formula:
     * Saldo para planejamento = receitas recebidas + entradas adicionais disponíveis
     *                           - despesas realizadas - contas pendentes reservadas
     *                           - valor reservado para economia.
     * Limite diário recomendado = saldo para planejamento ÷ dias restantes até o próximo recebimento.
     */
    public DailyLimitDto calculateDailyLimit(User user) {
        LocalDate today = LocalDate.now();
        DailyLimitDto dto = new DailyLimitDto();

        // 1. Calculate days until next salary
        int salaryDay = user.getSalaryDay() != null ? user.getSalaryDay() : 5;
        LocalDate nextSalaryDate;
        int currentDay = today.getDayOfMonth();

        if (currentDay < salaryDay) {
            int safeDay = Math.min(salaryDay, today.lengthOfMonth());
            nextSalaryDate = LocalDate.of(today.getYear(), today.getMonthValue(), safeDay);
        } else {
            LocalDate nextMonth = today.plusMonths(1);
            int safeDay = Math.min(salaryDay, nextMonth.lengthOfMonth());
            nextSalaryDate = LocalDate.of(nextMonth.getYear(), nextMonth.getMonthValue(), safeDay);
        }

        long daysRemaining = ChronoUnit.DAYS.between(today, nextSalaryDate);
        if (daysRemaining <= 0) {
            daysRemaining = 1;
        }
        dto.setDaysUntilNextSalary(daysRemaining);
        dto.setNextSalaryDate(nextSalaryDate);

        // Current Month Cycle
        YearMonth ym = YearMonth.of(today.getYear(), today.getMonthValue());
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();

        // 2. Receitas Recebidas
        BigDecimal receivedIncomes = incomeRepository.sumAmountByUserIdAndReceiptStatusAndReceiptDateBetween(
                user.getId(), ReceiptStatus.RECEBIDO, startOfMonth, endOfMonth);
        if (receivedIncomes == null) receivedIncomes = BigDecimal.ZERO;

        BigDecimal baseSalary = user.getMonthlyIncome() != null ? user.getMonthlyIncome() : BigDecimal.ZERO;
        BigDecimal totalReceitasDisponiveis = receivedIncomes.compareTo(BigDecimal.ZERO) > 0 ? receivedIncomes : baseSalary;

        // 3. Despesas Realizadas (PAGAS)
        BigDecimal despesasRealizadas = expenseRepository.sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
                user.getId(), PaymentStatus.PAGO, startOfMonth, endOfMonth);
        if (despesasRealizadas == null) despesasRealizadas = BigDecimal.ZERO;

        // 4. Contas Pendentes Reservadas (PENDENTES)
        BigDecimal contasPendentes = expenseRepository.sumAmountByUserIdAndPaymentStatus(
                user.getId(), PaymentStatus.PENDENTE);
        if (contasPendentes == null) contasPendentes = BigDecimal.ZERO;

        // 5. Valor Reservado para Economia
        BigDecimal valorReservadoEconomia = user.getSavingsGoalMonthly() != null ? user.getSavingsGoalMonthly() : BigDecimal.ZERO;

        // Check if user has sufficient data
        if (totalReceitasDisponiveis.compareTo(BigDecimal.ZERO) <= 0 &&
                user.getCurrentBankBalance().compareTo(BigDecimal.ZERO) <= 0) {
            dto.setHasSufficientData(false);
            dto.setExplanationMessage("Para calcular seu limite diário com precisão, informe sua renda mensal ou cadastre suas receitas no sistema.");
            return dto;
        }

        // 6. Saldo para Planejamento
        BigDecimal saldoPlanejamento = totalReceitasDisponiveis
                .subtract(despesasRealizadas)
                .subtract(contasPendentes)
                .subtract(valorReservadoEconomia);

        dto.setPlanningAvailableBalance(saldoPlanejamento);

        // 7. Limite Diário Recomendado
        BigDecimal limiteDiario;
        if (saldoPlanejamento.compareTo(BigDecimal.ZERO) > 0) {
            limiteDiario = saldoPlanejamento.divide(BigDecimal.valueOf(daysRemaining), 2, RoundingMode.HALF_UP);
        } else {
            limiteDiario = BigDecimal.ZERO;
        }
        dto.setRecommendedDailyLimit(limiteDiario);
        dto.setRecommendedWeeklyLimit(limiteDiario.multiply(BigDecimal.valueOf(7)));

        // 8. Gastos de Hoje
        BigDecimal gastoHoje = expenseRepository.sumPaidAmountByUserIdAndDate(user.getId(), today);
        if (gastoHoje == null) gastoHoje = BigDecimal.ZERO;
        dto.setSpentToday(gastoHoje);

        BigDecimal restanteHoje = limiteDiario.subtract(gastoHoje);
        dto.setRemainingToday(restanteHoje);

        // 9. Status de Saúde Orçamentária
        if (limiteDiario.compareTo(BigDecimal.ZERO) == 0 && saldoPlanejamento.compareTo(BigDecimal.ZERO) <= 0) {
            dto.setBudgetHealthStatus("VERMELHO");
            dto.setHealthMessage("Atenção crítica: seu saldo disponível para planejamento está zerado ou negativo.");
        } else if (restanteHoje.compareTo(BigDecimal.ZERO) < 0) {
            dto.setBudgetHealthStatus("VERMELHO");
            dto.setHealthMessage(String.format("Limite diário ultrapassado em R$ %s. Evite novos gastos hoje.", restanteHoje.abs().setScale(2, RoundingMode.HALF_UP)));
        } else if (gastoHoje.compareTo(limiteDiario.multiply(new BigDecimal("0.80"))) >= 0) {
            dto.setBudgetHealthStatus("AMARELO");
            dto.setHealthMessage("Atenção: você já utilizou mais de 80% do seu limite diário recomendado para hoje.");
        } else {
            dto.setBudgetHealthStatus("VERDE");
            dto.setHealthMessage("Excelente! Seus gastos de hoje estão perfeitamente equilibrados dentro do planejamento.");
        }

        dto.setExplanationMessage("Lembre-se: este limite é uma recomendação de segurança para seu planejamento mensal, e não uma meta para gastar todo o valor.");
        return dto;
    }

    /**
     * Generates complete dashboard summary for selected timeframe:
     * "hoje", "semana", "mes", "3meses", "6meses", "ano"
     */
    public DashboardSummaryDto getDashboardSummary(User user, String period) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate = today;

        if ("hoje".equalsIgnoreCase(period)) {
            startDate = today;
        } else if ("semana".equalsIgnoreCase(period)) {
            startDate = today.minusDays(6);
        } else if ("3meses".equalsIgnoreCase(period)) {
            startDate = today.minusMonths(3);
        } else if ("6meses".equalsIgnoreCase(period)) {
            startDate = today.minusMonths(6);
        } else if ("ano".equalsIgnoreCase(period)) {
            startDate = today.minusYears(1);
        } else { // default "mes"
            startDate = today.withDayOfMonth(1);
            endDate = today.withDayOfMonth(today.lengthOfMonth());
        }

        DashboardSummaryDto dto = new DashboardSummaryDto();

        // 1. Monthly income
        BigDecimal monthlyIncome = user.getMonthlyIncome() != null ? user.getMonthlyIncome() : BigDecimal.ZERO;
        dto.setMonthlyIncome(monthlyIncome);

        // 2. User bank balance
        dto.setUserBankBalance(user.getCurrentBankBalance() != null ? user.getCurrentBankBalance() : BigDecimal.ZERO);

        // 3. Total paid expenses in timeframe
        BigDecimal totalExpenses = expenseRepository.sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
                user.getId(), PaymentStatus.PAGO, startDate, endDate);
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;
        dto.setTotalExpenses(totalExpenses);

        // 4. Pending bills total
        BigDecimal pendingBills = expenseRepository.sumAmountByUserIdAndPaymentStatus(
                user.getId(), PaymentStatus.PENDENTE);
        if (pendingBills == null) pendingBills = BigDecimal.ZERO;
        dto.setPendingBillsTotal(pendingBills);

        // 5. Monthly savings goal
        BigDecimal savingsGoal = user.getSavingsGoalMonthly() != null ? user.getSavingsGoalMonthly() : BigDecimal.ZERO;
        dto.setMonthlySavingsGoal(savingsGoal);

        // 6. Reserved for savings (sum from active financial goals or user goal)
        BigDecimal totalGoalAccumulated = goalRepository.sumCurrentAmountByUserId(user.getId());
        dto.setReservedForSavings(totalGoalAccumulated != null && totalGoalAccumulated.compareTo(BigDecimal.ZERO) > 0 ?
                totalGoalAccumulated : savingsGoal);

        // 7. Income used percentage
        if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pct = totalExpenses.multiply(new BigDecimal("100")).divide(monthlyIncome, 1, RoundingMode.HALF_UP);
            dto.setIncomeUsedPercentage(pct);
        } else {
            dto.setIncomeUsedPercentage(BigDecimal.ZERO);
        }

        // Daily limit snapshot
        DailyLimitDto dailyLimitDto = calculateDailyLimit(user);
        dto.setDailyLimit(dailyLimitDto);
        dto.setPlanningAvailableBalance(dailyLimitDto.getPlanningAvailableBalance());

        // 8. Categories breakdown
        List<Object[]> categoryData = expenseRepository.findCategoryExpensesBetween(user.getId(), startDate, endDate);
        for (Object[] row : categoryData) {
            String name = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            String color = row[2] != null ? (String) row[2] : "#4A5568";
            BigDecimal pct = BigDecimal.ZERO;
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                pct = amount.multiply(new BigDecimal("100")).divide(totalExpenses, 1, RoundingMode.HALF_UP);
            }
            dto.getCategoryExpenses().add(new DashboardSummaryDto.CategoryExpenseData(name, amount, pct, color));
        }

        // 9. Daily evolution (last 14 days or days in period)
        LocalDate dayCursor = startDate.isBefore(today.minusDays(14)) ? today.minusDays(14) : startDate;
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd/MM");
        while (!dayCursor.isAfter(endDate)) {
            BigDecimal daySpent = expenseRepository.sumPaidAmountByUserIdAndDate(user.getId(), dayCursor);
            dto.getDailyEvolution().add(new DashboardSummaryDto.DailyExpenseData(
                    dayCursor.format(dayFmt),
                    daySpent != null ? daySpent : BigDecimal.ZERO
            ));
            dayCursor = dayCursor.plusDays(1);
        }

        // 10. Weekly comparison (last 4 weeks)
        for (int w = 3; w >= 0; w--) {
            LocalDate wStart = today.minusWeeks(w).minusDays(today.getDayOfWeek().getValue() - 1);
            LocalDate wEnd = wStart.plusDays(6);
            BigDecimal wSpent = expenseRepository.sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
                    user.getId(), PaymentStatus.PAGO, wStart, wEnd);
            String label = "Semana " + (4 - w);
            dto.getWeeklyComparison().add(new DashboardSummaryDto.WeeklyExpenseData(
                    label, wSpent != null ? wSpent : BigDecimal.ZERO));
        }

        // 11. Income vs Expense
        BigDecimal periodIncome = incomeRepository.sumAmountByUserIdAndReceiptStatusAndReceiptDateBetween(
                user.getId(), ReceiptStatus.RECEBIDO, startDate, endDate);
        if (periodIncome == null || periodIncome.compareTo(BigDecimal.ZERO) == 0) {
            periodIncome = monthlyIncome;
        }
        dto.getIncomeVsExpense().setTotalIncome(periodIncome);
        dto.getIncomeVsExpense().setTotalExpense(totalExpenses);
        dto.getIncomeVsExpense().setNetBalance(periodIncome.subtract(totalExpenses));

        // 12. Planned vs Realized
        final BigDecimal effectivePeriodIncome = periodIncome;
        final BigDecimal effectiveTotalExpenses = totalExpenses;
        budgetRepository.findByUserIdAndMonthAndYear(user.getId(), today.getMonthValue(), today.getYear())
                .ifPresentOrElse(b -> {
                    BigDecimal plannedInc = b.getExpectedIncome().add(b.getExpectedExtraIncome());
                    BigDecimal plannedExp = b.getFixedExpensesBudget().add(b.getVariableExpensesBudget());
                    dto.setPlannedVsRealized(new DashboardSummaryDto.PlannedVsRealizedData(
                            plannedInc, effectivePeriodIncome,
                            plannedExp, effectiveTotalExpenses
                    ));
                }, () -> {
                    dto.setPlannedVsRealized(new DashboardSummaryDto.PlannedVsRealizedData(
                            monthlyIncome, effectivePeriodIncome,
                            monthlyIncome.multiply(new BigDecimal("0.80")).setScale(2, RoundingMode.HALF_UP), effectiveTotalExpenses
                    ));
                });

        // 13. Savings evolution (last 6 months)
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM/yy");
        for (int m = 5; m >= 0; m--) {
            YearMonth pastYm = YearMonth.from(today.minusMonths(m));
            LocalDate mStart = pastYm.atDay(1);
            LocalDate mEnd = pastYm.atEndOfMonth();

            BigDecimal mInc = incomeRepository.sumAmountByUserIdAndReceiptStatusAndReceiptDateBetween(
                    user.getId(), ReceiptStatus.RECEBIDO, mStart, mEnd);
            BigDecimal mExp = expenseRepository.sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
                    user.getId(), PaymentStatus.PAGO, mStart, mEnd);

            if (mInc == null) mInc = BigDecimal.ZERO;
            if (mExp == null) mExp = BigDecimal.ZERO;
            BigDecimal saved = mInc.subtract(mExp).max(BigDecimal.ZERO);

            dto.getSavingsEvolution().add(new DashboardSummaryDto.MonthlySavingsData(
                    pastYm.format(monthFmt), saved
            ));
        }

        return dto;
    }
}
