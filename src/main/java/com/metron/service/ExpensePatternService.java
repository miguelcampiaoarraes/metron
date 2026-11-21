package com.metron.service;

import com.metron.model.entity.Category;
import com.metron.model.entity.Expense;
import com.metron.model.entity.PaymentStatus;
import com.metron.model.entity.User;
import com.metron.repository.CategoryRepository;
import com.metron.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpensePatternService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpensePatternService(ExpenseRepository expenseRepository,
                                 CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    public static class PatternInsight {
        private String type; // MICRO_EXPENSE, RECURRING_SUBSCRIPTION, CATEGORY_SPIKE, BUDGET_EXCEEDED
        private String title;
        private String message;
        private BigDecimal impactAmount;
        private String severity; // INFO, WARNING, DANGER

        public PatternInsight(String type, String title, String message, BigDecimal impactAmount, String severity) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.impactAmount = impactAmount;
            this.severity = severity;
        }

        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public BigDecimal getImpactAmount() { return impactAmount; }
        public String getSeverity() { return severity; }
    }

    public List<PatternInsight> analyzePatterns(User user) {
        List<PatternInsight> insights = new ArrayList<>();
        LocalDate today = LocalDate.now();
        YearMonth ym = YearMonth.of(today.getYear(), today.getMonthValue());
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();

        List<Expense> monthExpenses = expenseRepository.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(
                user.getId(), startOfMonth, endOfMonth);

        // 1. Identify Frequent Micro-Expenses (amount <= 30 and count >= 5)
        List<Expense> microExpenses = monthExpenses.stream()
                .filter(e -> e.getPaymentStatus() == PaymentStatus.PAGO &&
                        e.getAmount().compareTo(new BigDecimal("30.00")) <= 0)
                .collect(Collectors.toList());

        if (microExpenses.size() >= 5) {
            BigDecimal microTotal = microExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            insights.add(new PatternInsight(
                    "MICRO_EXPENSE",
                    "Atenção aos pequenos gastos diários",
                    String.format("Você realizou %d pequenos gastos este mês que somam R$ %s. Pequenas compras não planejadas no dia a dia podem comprometer seu orçamento sem você perceber.",
                            microExpenses.size(), microTotal.setScale(2, RoundingMode.HALF_UP)),
                    microTotal,
                    "WARNING"
            ));
        }

        // 2. Identify Recurring Subscriptions
        List<Expense> recurring = expenseRepository.findRecurringExpenses(user.getId());
        if (!recurring.isEmpty()) {
            BigDecimal recurringTotal = recurring.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            insights.add(new PatternInsight(
                    "RECURRING_SUBSCRIPTION",
                    "Assinaturas e despesas fixas recorrentes",
                    String.format("Você possui %d despesas recorrentes cadastradas totalizando R$ %s por mês. Recomendamos avaliar se todos os serviços ainda estão sendo utilizados.",
                            recurring.size(), recurringTotal.setScale(2, RoundingMode.HALF_UP)),
                    recurringTotal,
                    "INFO"
            ));
        }

        // 3. Category budget limit exceeded
        List<Category> userCategories = categoryRepository.findAllAvailableForUser(user.getId());
        Map<Long, BigDecimal> categoryLimits = userCategories.stream()
                .filter(c -> c.getBudgetLimit() != null && c.getBudgetLimit().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toMap(Category::getId, Category::getBudgetLimit, (a, b) -> a));

        Map<Long, BigDecimal> categorySpent = new HashMap<>();
        for (Expense e : monthExpenses) {
            if (e.getPaymentStatus() == PaymentStatus.PAGO && e.getCategory() != null) {
                categorySpent.merge(e.getCategory().getId(), e.getAmount(), BigDecimal::add);
            }
        }

        for (Map.Entry<Long, BigDecimal> entry : categoryLimits.entrySet()) {
            Long catId = entry.getKey();
            BigDecimal limit = entry.getValue();
            BigDecimal spent = categorySpent.getOrDefault(catId, BigDecimal.ZERO);

            if (spent.compareTo(limit) > 0) {
                Category cat = userCategories.stream().filter(c -> c.getId().equals(catId)).findFirst().orElse(null);
                String catName = cat != null ? cat.getName() : "Categoria";
                BigDecimal excess = spent.subtract(limit);
                insights.add(new PatternInsight(
                        "BUDGET_EXCEEDED",
                        "Limite de categoria ultrapassado: " + catName,
                        String.format("Você gastou R$ %s com %s neste mês, enquanto seu limite planejado era R$ %s (excedido em R$ %s).",
                                spent.setScale(2, RoundingMode.HALF_UP), catName,
                                limit.setScale(2, RoundingMode.HALF_UP), excess.setScale(2, RoundingMode.HALF_UP)),
                        excess,
                        "DANGER"
                ));
            }
        }

        return insights;
    }
}
