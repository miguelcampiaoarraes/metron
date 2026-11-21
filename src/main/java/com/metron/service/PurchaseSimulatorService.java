package com.metron.service;

import com.metron.dto.DailyLimitDto;
import com.metron.dto.PurchaseSimulationRequest;
import com.metron.dto.PurchaseSimulationResponse;
import com.metron.model.entity.Budget;
import com.metron.model.entity.PaymentStatus;
import com.metron.model.entity.User;
import com.metron.repository.BudgetRepository;
import com.metron.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class PurchaseSimulatorService {

    private final FinancialEngineService engineService;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public PurchaseSimulatorService(FinancialEngineService engineService,
                                    ExpenseRepository expenseRepository,
                                    BudgetRepository budgetRepository) {
        this.engineService = engineService;
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }

    public PurchaseSimulationResponse simulatePurchase(User user, PurchaseSimulationRequest request) {
        BigDecimal purchaseAmount = request.getAmount();
        DailyLimitDto dailyLimit = engineService.calculateDailyLimit(user);

        PurchaseSimulationResponse response = new PurchaseSimulationResponse();
        response.setPurchaseAmount(purchaseAmount);
        response.setCurrentDailyLimit(dailyLimit.getRecommendedDailyLimit());

        // Impact on daily limit
        BigDecimal remainingToday = dailyLimit.getRemainingToday();
        BigDecimal newRemainingToday = remainingToday.subtract(purchaseAmount);
        response.setRemainingDailyAfterPurchase(newRemainingToday);

        boolean overDailyLimit = newRemainingToday.compareTo(BigDecimal.ZERO) < 0;
        BigDecimal excess = overDailyLimit ? newRemainingToday.abs() : BigDecimal.ZERO;
        response.setExcessOverDailyLimit(excess);

        // Impact on monthly planning balance
        BigDecimal planningBalance = dailyLimit.getPlanningAvailableBalance();
        BigDecimal newPlanningBalance = planningBalance.subtract(purchaseAmount);

        // Monthly budget evaluation
        LocalDate today = LocalDate.now();
        YearMonth ym = YearMonth.of(today.getYear(), today.getMonthValue());
        Budget budget = budgetRepository.findByUserIdAndMonthAndYear(user.getId(), ym.getMonthValue(), ym.getYear())
                .orElse(null);

        BigDecimal monthlyLimit = budget != null ?
                budget.getFixedExpensesBudget().add(budget.getVariableExpensesBudget()) :
                user.getMonthlyIncome().multiply(new BigDecimal("0.80")).setScale(2, RoundingMode.HALF_UP);

        BigDecimal currentMonthExpenses = expenseRepository.sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
                user.getId(), PaymentStatus.PAGO, ym.atDay(1), ym.atEndOfMonth());
        if (currentMonthExpenses == null) currentMonthExpenses = BigDecimal.ZERO;

        BigDecimal remainingMonthly = monthlyLimit.subtract(currentMonthExpenses);
        response.setCurrentMonthlyBudget(monthlyLimit);
        response.setRemainingMonthlyBudgetAfterPurchase(remainingMonthly.subtract(purchaseAmount));

        // Impact on savings goal
        BigDecimal monthlySavingsGoal = user.getSavingsGoalMonthly() != null ? user.getSavingsGoalMonthly() : BigDecimal.ZERO;
        boolean savingsCompromised = newPlanningBalance.compareTo(BigDecimal.ZERO) < 0;
        response.setSavingsCompromised(savingsCompromised);
        response.setPotentialSavingsReduction(savingsCompromised ? newPlanningBalance.abs().min(monthlySavingsGoal) : BigDecimal.ZERO);

        // Impact on pending bills
        BigDecimal pendingBills = expenseRepository.sumAmountByUserIdAndPaymentStatus(user.getId(), PaymentStatus.PENDENTE);
        if (pendingBills == null) pendingBills = BigDecimal.ZERO;
        boolean pendingBillsAtRisk = user.getCurrentBankBalance().add(planningBalance).subtract(purchaseAmount).compareTo(pendingBills) < 0;
        response.setPendingBillsAtRisk(pendingBillsAtRisk);

        // Determine Verdict and Explanations
        if (!overDailyLimit && !savingsCompromised && !pendingBillsAtRisk) {
            response.setAffordable(true);
            response.setVerdict("APROVADO");
            response.setSummaryMessage(String.format(
                    "Sim, a compra de R$ %s cabe com tranquilidade no seu planejamento diário e mensal.",
                    formatMoney(purchaseAmount)
            ));
            response.setDetailedExplanation(String.format(
                    "Após essa compra, você ainda terá R$ %s disponíveis hoje. Suas contas pendentes e sua meta de economia continuam 100%% preservadas.",
                    formatMoney(newRemainingToday)
            ));
        } else if (overDailyLimit && !pendingBillsAtRisk && newPlanningBalance.compareTo(BigDecimal.ZERO) >= 0) {
            response.setAffordable(true);
            response.setVerdict("ATENCAO");
            response.setSummaryMessage(String.format(
                    "Essa compra de R$ %s ultrapassaria seu limite diário recomendado em R$ %s. Se você realizar a compra, precisará compensar nos próximos dias.",
                    formatMoney(purchaseAmount),
                    formatMoney(excess)
            ));
            response.setDetailedExplanation(String.format(
                    "A compra cabe no saldo geral do mês, mas reduzirá seu limite diário nos próximos %d dias para compensar os R$ %s excedidos hoje.",
                    dailyLimit.getDaysUntilNextSalary(),
                    formatMoney(excess)
            ));
        } else {
            response.setAffordable(false);
            response.setVerdict("DESACONSELHADO");
            response.setSummaryMessage(String.format(
                    "Essa compra de R$ %s não é recomendada agora, pois comprometerá seu planejamento financeiro essencial.",
                    formatMoney(purchaseAmount)
            ));

            StringBuilder sb = new StringBuilder();
            if (pendingBillsAtRisk) {
                sb.append("A compra coloca em risco o pagamento de contas essenciais já agendadas (R$ ").append(formatMoney(pendingBills)).append("). ");
            }
            if (savingsCompromised) {
                sb.append("Além disso, reduzirá sua reserva de economia mensal em R$ ").append(formatMoney(response.getPotentialSavingsReduction())).append(". ");
            }
            sb.append("Recomendamos adiar ou pesquisar alternativas mais econômicas.");
            response.setDetailedExplanation(sb.toString());
        }

        return response;
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0,00";
        return amount.setScale(2, RoundingMode.HALF_UP).toString().replace(".", ",");
    }
}
