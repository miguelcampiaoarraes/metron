package com.metron.service;

import com.metron.dto.DailyLimitDto;
import com.metron.dto.FinancialAssistantResponse;
import com.metron.model.entity.FinancialGoal;
import com.metron.model.entity.GoalStatus;
import com.metron.model.entity.PaymentStatus;
import com.metron.model.entity.ReceiptStatus;
import com.metron.model.entity.User;
import com.metron.repository.ExpenseRepository;
import com.metron.repository.FinancialGoalRepository;
import com.metron.repository.IncomeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

@Service
public class FinancialAssistantService {

    private final FinancialEngineService engineService;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final FinancialGoalRepository goalRepository;

    @Value("${metron.ai.gemini-api-key:}")
    private String geminiApiKey;

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0,00";
        return String.format(LOCALE_BR, "%,.2f", amount.setScale(2, RoundingMode.HALF_UP));
    }

    public FinancialAssistantService(FinancialEngineService engineService,
                                     ExpenseRepository expenseRepository,
                                     IncomeRepository incomeRepository,
                                     FinancialGoalRepository goalRepository) {
        this.engineService = engineService;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.goalRepository = goalRepository;
    }

    public FinancialAssistantResponse answer(User user, String question) {
        String cleanQuestion = question != null ? question.trim() : "";
        if (cleanQuestion.equalsIgnoreCase("diagnostico_geral")) {
            cleanQuestion = "Como está a saúde geral das minhas finanças?";
        } else if (cleanQuestion.equalsIgnoreCase("onde_estou_gastando")) {
            cleanQuestion = "Onde estou gastando mais dinheiro este mês?";
        } else if (cleanQuestion.equalsIgnoreCase("quanto_posso_gastar_hoje")) {
            cleanQuestion = "Quanto posso gastar hoje com total segurança?";
        } else if (cleanQuestion.equalsIgnoreCase("como_economizar")) {
            cleanQuestion = "Quais são as melhores formas de economizar agora?";
        } else if (cleanQuestion.equalsIgnoreCase("contas_futuras")) {
            cleanQuestion = "Minhas contas pendentes do mês estão seguras?";
        } else if (cleanQuestion.equalsIgnoreCase("ritmo_gastos")) {
            cleanQuestion = "Meu ritmo de gastos está dentro do planejado?";
        }
        String lower = cleanQuestion.toLowerCase();

        // 1. Compute Deterministic Financial State
        DailyLimitDto dailyLimit = engineService.calculateDailyLimit(user);
        LocalDate today = LocalDate.now();
        YearMonth ym = YearMonth.of(today.getYear(), today.getMonthValue());
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();

        BigDecimal monthlyIncome = user.getMonthlyIncome() != null ? user.getMonthlyIncome() : BigDecimal.ZERO;
        BigDecimal receivedIncome = incomeRepository.sumAmountByUserIdAndReceiptStatusAndReceiptDateBetween(
                user.getId(), ReceiptStatus.RECEBIDO, startOfMonth, endOfMonth);
        if (receivedIncome == null || receivedIncome.compareTo(BigDecimal.ZERO) == 0) {
            receivedIncome = monthlyIncome;
        }

        BigDecimal paidExpenses = expenseRepository.sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
                user.getId(), PaymentStatus.PAGO, startOfMonth, endOfMonth);
        if (paidExpenses == null) paidExpenses = BigDecimal.ZERO;

        BigDecimal pendingBills = expenseRepository.sumAmountByUserIdAndPaymentStatus(
                user.getId(), PaymentStatus.PENDENTE);
        if (pendingBills == null) pendingBills = BigDecimal.ZERO;

        BigDecimal savingsGoal = user.getSavingsGoalMonthly() != null ? user.getSavingsGoalMonthly() : BigDecimal.ZERO;

        List<Object[]> topCategories = expenseRepository.findCategoryExpensesBetween(user.getId(), startOfMonth, endOfMonth);
        String topCatName = "Despesas gerais";
        BigDecimal topCatAmount = BigDecimal.ZERO;
        BigDecimal topCatPct = BigDecimal.ZERO;
        if (!topCategories.isEmpty()) {
            topCatName = (String) topCategories.get(0)[0];
            topCatAmount = (BigDecimal) topCategories.get(0)[1];
            if (paidExpenses.compareTo(BigDecimal.ZERO) > 0) {
                topCatPct = topCatAmount.multiply(new BigDecimal("100")).divide(paidExpenses, 1, RoundingMode.HALF_UP);
            }
        }

        List<FinancialGoal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        // 2. Dispatch structured rule-based deterministic response
        if (lower.contains("hoje") || lower.contains("posso gastar")) {
            return buildQuantoPossoGastarHoje(cleanQuestion, dailyLimit);
        } else if (lower.contains("onde") || lower.contains("gastando mais") || lower.contains("maiores gastos")) {
            return buildOndeEstouGastandoMais(cleanQuestion, topCatName, topCatAmount, topCatPct, paidExpenses);
        } else if (lower.contains("juntar") || lower.contains("poupar") || lower.contains("economizar mais") || lower.contains("como economizar")) {
            return buildComoJuntarMaisDinheiro(cleanQuestion, monthlyIncome, paidExpenses, savingsGoal, topCatName, topCatAmount);
        } else if (lower.contains("reduzir") || lower.contains("cortar")) {
            return buildQuaisDespesasReduzir(cleanQuestion, topCatName, topCatAmount, topCatPct);
        } else if (lower.contains("sobrar") || lower.contains("final do mês") || lower.contains("contas") || lower.contains("futuras")) {
            return buildQuantoSobrara(cleanQuestion, receivedIncome, paidExpenses, pendingBills, savingsGoal, dailyLimit);
        } else if (lower.contains("distribuir") || lower.contains("dividir") || lower.contains("salário") || lower.contains("salario")) {
            return buildComoDistribuirSalario(cleanQuestion, monthlyIncome);
        } else if (lower.contains("meta") || lower.contains("alcançar")) {
            return buildQuantoPrecisoEconomizarMeta(cleanQuestion, goals, savingsGoal);
        } else if (lower.contains("aumentaram") || lower.contains("cresceram") || lower.contains("ritmo")) {
            return buildQuaisGastosAumentaram(cleanQuestion, topCatName, topCatAmount, topCatPct);
        } else {
            return buildDiagnosticoGeral(cleanQuestion, dailyLimit, receivedIncome, paidExpenses, pendingBills, savingsGoal, topCatName, topCatAmount);
        }
    }

    private FinancialAssistantResponse buildQuantoPossoGastarHoje(String q, DailyLimitDto limit) {
        String analysis = String.format("Seu saldo livre para planejamento até o próximo recebimento é de R$ %s, restando %d dias.",
                formatMoney(limit.getPlanningAvailableBalance()), limit.getDaysUntilNextSalary());
        String data = String.format("Saldo para planejamento: R$ %s | Dias restantes: %d | Gastos registrados hoje: R$ %s.",
                formatMoney(limit.getPlanningAvailableBalance()), limit.getDaysUntilNextSalary(), formatMoney(limit.getSpentToday()));
        String calc = String.format("R$ %s ÷ %d dias = R$ %s por dia. Subtraindo R$ %s gastos hoje = R$ %s restantes hoje.",
                formatMoney(limit.getPlanningAvailableBalance()), limit.getDaysUntilNextSalary(),
                formatMoney(limit.getRecommendedDailyLimit()), formatMoney(limit.getSpentToday()),
                formatMoney(limit.getRemainingToday()));
        String rec = limit.getRemainingToday().compareTo(BigDecimal.ZERO) >= 0 ?
                String.format("Você ainda pode gastar até R$ %s com segurança hoje sem comprometer suas contas ou metas.", formatMoney(limit.getRemainingToday())) :
                String.format("Você excedeu o limite diário recomendado em R$ %s. Recomendamos suspender despesas extras hoje.", formatMoney(limit.getRemainingToday().abs()));
        String savings = "Evitando gastar acima do limite diário, você preserva 100% da sua meta mensal.";
        String nextAction = "Consulte o Simulador de Compras da METRON antes de realizar qualquer compra não essencial.";

        return new FinancialAssistantResponse(q, analysis, data, calc, rec, savings, nextAction, false);
    }

    private FinancialAssistantResponse buildOndeEstouGastandoMais(String q, String topCat, BigDecimal topAmount, BigDecimal topPct, BigDecimal totalSpent) {
        String analysis = String.format("A categoria com maior concentração dos seus gastos neste mês é '%s'.", topCat);
        String data = String.format("Gasto em '%s': R$ %s | Total de despesas pagas no mês: R$ %s.",
                topCat, formatMoney(topAmount), formatMoney(totalSpent));
        String calc = String.format("(R$ %s ÷ R$ %s) × 100 = %.1f%% do seu orçamento mensal realizado.",
                formatMoney(topAmount), formatMoney(totalSpent), topPct.doubleValue());
        String rec = String.format("Concentre sua atenção em otimizar gastos em '%s'. Mesmo pequenas economias percentuais nessa categoria geram alto impacto financeiro.", topCat);
        BigDecimal est = topAmount.multiply(new BigDecimal("0.15")).setScale(2, RoundingMode.HALF_UP);
        String savings = String.format("Reduzindo apenas 15%% dos gastos com '%s', você economizará aproximadamente R$ %s neste mês.", topCat, formatMoney(est));
        String nextAction = String.format("Acesse a aba 'Despesas', filtre por '%s' e identifique itens não essenciais.", topCat);

        return new FinancialAssistantResponse(q, analysis, data, calc, rec, savings, nextAction, false);
    }

    private FinancialAssistantResponse buildComoJuntarMaisDinheiro(String q, BigDecimal income, BigDecimal spent, BigDecimal savingsGoal, String topCat, BigDecimal topAmount) {
        BigDecimal balance = income.subtract(spent);
        String analysis = "Para juntar mais dinheiro de forma sustentável, a regra de ouro é automatizar sua economia antes de iniciar os gastos do mês.";
        String data = String.format("Renda informada: R$ %s | Total de gastos no mês: R$ %s | Meta de economia atual: R$ %s.",
                formatMoney(income), formatMoney(spent), formatMoney(savingsGoal));
        BigDecimal recSavings = income.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
        String calc = String.format("Recomendação METRON: reservar 20%% da sua renda (20%% de R$ %s = R$ %s).",
                formatMoney(income), formatMoney(recSavings));
        String rec = "Aplique o princípio 'Pague-se Primeiro': separe o valor da sua reserva imediatamente no dia em que o salário cair na conta, e viva com os 80% restantes.";
        String savings = String.format("Em 6 meses guardando R$ %s/mês, você acumulará R$ %s em sua reserva.",
                formatMoney(recSavings), formatMoney(recSavings.multiply(BigDecimal.valueOf(6))));
        String nextAction = "Crie uma meta financeira em 'Metas' com o valor desejado para os próximos 6 meses.";

        return new FinancialAssistantResponse(q, analysis, data, calc, rec, savings, nextAction, false);
    }

    private FinancialAssistantResponse buildQuaisDespesasReduzir(String q, String topCat, BigDecimal topAmount, BigDecimal topPct) {
        String analysis = String.format("O maior potencial de corte sem afetar a qualidade de vida está nas despesas discricionárias da categoria '%s'.", topCat);
        String data = String.format("Gasto atual em '%s': R$ %s (%.1f%% de todas as despesas).",
                topCat, formatMoney(topAmount), topPct.doubleValue());
        BigDecimal red20 = topAmount.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
        String calc = String.format("Meta de redução de 20%%: R$ %s × 0,20 = R$ %s de alívio no caixa.",
                formatMoney(topAmount), formatMoney(red20));
        String rec = "Revise compras por impulso, lanches fora de hora e assinaturas automáticas que você raramente utiliza.";
        String savings = String.format("Economia estimada de R$ %s por mês (R$ %s ao ano).",
                formatMoney(red20), formatMoney(red20.multiply(BigDecimal.valueOf(12))));
        String nextAction = "Defina um limite de orçamento mensal para essa categoria na tela 'Categorias'.";

        return new FinancialAssistantResponse(q, analysis, data, calc, rec, savings, nextAction, false);
    }

    private FinancialAssistantResponse buildQuantoSobrara(String q, BigDecimal income, BigDecimal spent, BigDecimal pending, BigDecimal savings, DailyLimitDto limit) {
        BigDecimal totalCommitted = spent.add(pending).add(savings);
        BigDecimal projectedSurplus = income.subtract(totalCommitted);

        String analysis = "Projeção com base em receitas recebidas, despesas já pagas, contas pendentes e reserva de economia.";
        String data = String.format("Renda: R$ %s | Gastos pagos: R$ %s | Contas pendentes: R$ %s | Reserva: R$ %s.",
                formatMoney(income), formatMoney(spent),
                formatMoney(pending), formatMoney(savings));
        String calc = String.format("R$ %s - (R$ %s + R$ %s + R$ %s) = R$ %s projetados.",
                formatMoney(income), formatMoney(spent),
                formatMoney(pending), formatMoney(savings),
                formatMoney(projectedSurplus));
        String rec = projectedSurplus.compareTo(BigDecimal.ZERO) >= 0 ?
                String.format("Seguindo seu planejamento, sobrarão R$ %s até o fim do mês, além da sua reserva de R$ %s já garantida.",
                        formatMoney(projectedSurplus), formatMoney(savings)) :
                String.format("Atenção: há um déficit projetado de R$ %s. É essencial conter gastos discricionários para não entrar no cheque especial.",
                        formatMoney(projectedSurplus.abs()));
        String savingsEst = "Mantendo o limite diário rigorosamente, sua meta de economia continuará intacta.";
        String nextAction = "Verifique na tela 'Planejamento Mensal' se há despesas variáveis passíveis de adiamento.";

        return new FinancialAssistantResponse(q, analysis, data, calc, rec, savingsEst, nextAction, false);
    }

    private FinancialAssistantResponse buildComoDistribuirSalario(String q, BigDecimal income) {
        BigDecimal fixed = income.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal variable = income.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal savings = income.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);

        String analysis = "A METRON recomenda a consagrada regra 50-30-20, adaptada para finanças brasileiras.";
        String data = String.format("Renda base considerada: R$ %s.", formatMoney(income));
        String calc = String.format("50%% Necessidades (R$ %s) | 30%% Estilo de vida (R$ %s) | 20%% Metas e Economia (R$ %s).",
                formatMoney(fixed), formatMoney(variable), formatMoney(savings));
        String rec = "Utilize no máximo 50% para moradia, contas básicas e alimentação; até 30% para compras pessoais e lazer; e garanta 20% guardados todo mês.";
        String savingsEst = String.format("Ao seguir essa regra, você poupa R$ %s todos os meses com equilíbrio e previsibilidade.", formatMoney(savings));
        String nextAction = "Configure seus limites de orçamento mensal na página 'Planejamento Mensal'.";

        return new FinancialAssistantResponse(q, analysis, data, calc, rec, savingsEst, nextAction, false);
    }

    private FinancialAssistantResponse buildQuantoPrecisoEconomizarMeta(String q, List<FinancialGoal> goals, BigDecimal currentMonthlySavings) {
        FinancialGoal activeGoal = goals.stream().filter(g -> g.getStatus() == GoalStatus.EM_ANDAMENTO).findFirst().orElse(null);
        if (activeGoal == null) {
            return new FinancialAssistantResponse(
                    q,
                    "Você ainda não possui metas financeiras ativas cadastradas.",
                    "Nenhuma meta ativa encontrada no banco de dados.",
                    "Cadastre uma meta informando o valor desejado e o prazo limite.",
                    "Cadastre seus objetivos (ex: reserva de emergência, viagem, aquisições) para receber cálculos automáticos de poupança mensal.",
                    "R$ 0,00",
                    "Acesse o menu 'Metas Financeiras' e crie seu primeiro objetivo.",
                    false
            );
        }

        BigDecimal remaining = activeGoal.getTargetAmount().subtract(activeGoal.getCurrentAmount()).max(BigDecimal.ZERO);
        String analysis = String.format("Análise da sua meta '%s': objetivo de R$ %s com R$ %s acumulados até agora.",
                activeGoal.getTitle(), formatMoney(activeGoal.getTargetAmount()),
                formatMoney(activeGoal.getCurrentAmount()));
        String data = String.format("Faltam R$ %s para concluir a meta.", formatMoney(remaining));
        String calc = String.format("R$ %s faltantes divididos pelo prazo de planejamento.", formatMoney(remaining));
        String rec = String.format("Direcione R$ %s/mês especificamente para esta meta para atingir o objetivo com pontualidade.",
                formatMoney(currentMonthlySavings.max(new BigDecimal("100"))));
        String savings = String.format("Meta de acumulação total: R$ %s.", formatMoney(remaining));
        String nextAction = "Utilize a função 'Aportar' na tela de Metas sempre que sobrar dinheiro ao final da semana.";

        return new FinancialAssistantResponse(q, analysis, data, calc, rec, savings, nextAction, false);
    }

    private FinancialAssistantResponse buildQuaisGastosAumentaram(String q, String topCat, BigDecimal topAmount, BigDecimal topPct) {
        String analysis = String.format("Seus registros apontam que a categoria '%s' apresentou a maior atividade recente.", topCat);
        String data = String.format("Total acumulado em '%s': R$ %s (%.1f%% do total do mês).",
                topCat, formatMoney(topAmount), topPct.doubleValue());
        String calc = String.format("Acompanhamento contínuo dos lançamentos na categoria '%s'.", topCat);
        String rec = "Avalie se as despesas recentes nessa categoria foram excepcionais ou se tornaram um novo padrão de custo fixo.";
        BigDecimal est = topAmount.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        String savings = String.format("Ajustando 10%% dos gastos dessa categoria, você recupera R$ %s por mês.", formatMoney(est));
        String nextAction = "Consulte o histórico detalhado filtrando por essa categoria.";

        return new FinancialAssistantResponse(q, analysis, data, calc, rec, savings, nextAction, false);
    }

    private FinancialAssistantResponse buildDiagnosticoGeral(String q, DailyLimitDto limit, BigDecimal income, BigDecimal spent, BigDecimal pending, BigDecimal savings, String topCat, BigDecimal topAmount) {
        String analysis = "Visão geral do seu momento financeiro na METRON: fluxo de caixa e segurança orçamentária calculados.";
        String data = String.format("Renda: R$ %s | Gastos realizados: R$ %s | Limite diário recomendado: R$ %s.",
                formatMoney(income), formatMoney(spent), formatMoney(limit.getRecommendedDailyLimit()));
        String calc = String.format("Saldo para planejamento: R$ %s dividido por %d dias restantes.",
                formatMoney(limit.getPlanningAvailableBalance()), limit.getDaysUntilNextSalary());
        String rec = "Mantenha o foco em respeitar o limite diário recomendado e garanta que suas contas pendentes sejam quitadas sem atraso.";
        String savingsTip = "O respeito ao limite diário assegura a conservação de toda a sua meta de economia mensal.";
        String nextAction = "Acompanhe seus indicadores no Dashboard e registre despesas no mesmo dia em que ocorrerem.";

        return new FinancialAssistantResponse(q, analysis, data, calc, rec, savingsTip, nextAction, false);
    }
}
