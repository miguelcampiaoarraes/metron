package com.metron;

import com.metron.dto.DailyLimitDto;
import com.metron.model.entity.PaymentStatus;
import com.metron.model.entity.ReceiptStatus;
import com.metron.model.entity.User;
import com.metron.repository.BudgetRepository;
import com.metron.repository.ExpenseRepository;
import com.metron.repository.FinancialGoalRepository;
import com.metron.repository.IncomeRepository;
import com.metron.service.FinancialEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialEngineServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private FinancialGoalRepository goalRepository;

    @InjectMocks
    private FinancialEngineService engineService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Carlos Silva");
        user.setEmail("carlos@metron.com");
        user.setMonthlyIncome(new BigDecimal("2000.00"));
        user.setCurrentBankBalance(new BigDecimal("2000.00"));
        user.setSavingsGoalMonthly(new BigDecimal("300.00"));
        // Set salary day so remaining days match test scenarios
        LocalDate today = LocalDate.now();
        LocalDate targetSalaryDate = today.plusDays(14);
        user.setSalaryDay(targetSalaryDate.getDayOfMonth());
    }

    @Test
    @DisplayName("Teste da Fórmula Master da METRON: Salário R$ 2000 - Despesas Pagas R$ 600 - Contas Pendentes R$ 400 - Meta R$ 300 = Saldo R$ 700 / 14 dias = R$ 50/dia")
    void testPromptReferenceFormula() {
        // Setup mock numbers from the prompt section 10
        when(incomeRepository.sumAmountByUserIdAndReceiptStatusAndReceiptDateBetween(
                eq(1L), eq(ReceiptStatus.RECEBIDO), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("2000.00"));

        when(expenseRepository.sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
                eq(1L), eq(PaymentStatus.PAGO), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("600.00"));

        when(expenseRepository.sumAmountByUserIdAndPaymentStatus(
                eq(1L), eq(PaymentStatus.PENDENTE)))
                .thenReturn(new BigDecimal("400.00"));

        when(expenseRepository.sumPaidAmountByUserIdAndDate(eq(1L), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        DailyLimitDto result = engineService.calculateDailyLimit(user);

        assertThat(result.isHasSufficientData()).isTrue();
        // Saldo para planejamento = 2000 - 600 - 400 - 300 = 700
        assertThat(result.getPlanningAvailableBalance()).isEqualByComparingTo(new BigDecimal("700.00"));
        assertThat(result.getDaysUntilNextSalary()).isEqualTo(14);
        // Limite diário recomendado = 700 / 14 = 50.00
        assertThat(result.getRecommendedDailyLimit()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getRecommendedWeeklyLimit()).isEqualByComparingTo(new BigDecimal("350.00"));
        assertThat(result.getRemainingToday()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getBudgetHealthStatus()).isEqualTo("VERDE");
    }

    @Test
    @DisplayName("Teste de Indicador Vermelho quando o limite diário recomendado é ultrapassado")
    void testDailyLimitExceededWarning() {
        when(incomeRepository.sumAmountByUserIdAndReceiptStatusAndReceiptDateBetween(
                eq(1L), eq(ReceiptStatus.RECEBIDO), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("2000.00"));

        when(expenseRepository.sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
                eq(1L), eq(PaymentStatus.PAGO), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("600.00"));

        when(expenseRepository.sumAmountByUserIdAndPaymentStatus(
                eq(1L), eq(PaymentStatus.PENDENTE)))
                .thenReturn(new BigDecimal("400.00"));

        // Gasto de R$ 80 hoje quando o limite diário é R$ 50
        when(expenseRepository.sumPaidAmountByUserIdAndDate(eq(1L), any(LocalDate.class)))
                .thenReturn(new BigDecimal("80.00"));

        DailyLimitDto result = engineService.calculateDailyLimit(user);

        assertThat(result.getRecommendedDailyLimit()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getSpentToday()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(result.getRemainingToday()).isEqualByComparingTo(new BigDecimal("-30.00"));
        assertThat(result.getBudgetHealthStatus()).isEqualTo("VERMELHO");
        assertThat(result.getHealthMessage()).contains("Limite diário ultrapassado");
    }

    @Test
    @DisplayName("Teste de Não Duplicação: Contas pendentes não devem ser contadas como despesas já pagas")
    void testNoDoubleCounting() {
        when(incomeRepository.sumAmountByUserIdAndReceiptStatusAndReceiptDateBetween(
                eq(1L), eq(ReceiptStatus.RECEBIDO), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1500.00"));

        // PAGO = 200, PENDENTE = 300
        when(expenseRepository.sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
                eq(1L), eq(PaymentStatus.PAGO), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("200.00"));

        when(expenseRepository.sumAmountByUserIdAndPaymentStatus(
                eq(1L), eq(PaymentStatus.PENDENTE)))
                .thenReturn(new BigDecimal("300.00"));

        when(expenseRepository.sumPaidAmountByUserIdAndDate(eq(1L), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        DailyLimitDto result = engineService.calculateDailyLimit(user);

        // 1500 - 200 (pago) - 300 (pendente) - 300 (reserva) = 700
        assertThat(result.getPlanningAvailableBalance()).isEqualByComparingTo(new BigDecimal("700.00"));
    }

    @Test
    @DisplayName("Teste de Tratamento quando Usuário não possui dados suficientes")
    void testInsufficientDataHandling() {
        User emptyUser = new User();
        emptyUser.setId(2L);
        emptyUser.setMonthlyIncome(BigDecimal.ZERO);
        emptyUser.setCurrentBankBalance(BigDecimal.ZERO);
        emptyUser.setSavingsGoalMonthly(BigDecimal.ZERO);

        when(incomeRepository.sumAmountByUserIdAndReceiptStatusAndReceiptDateBetween(
                eq(2L), eq(ReceiptStatus.RECEBIDO), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        DailyLimitDto result = engineService.calculateDailyLimit(emptyUser);

        assertThat(result.isHasSufficientData()).isFalse();
        assertThat(result.getExplanationMessage()).contains("informe sua renda mensal");
    }
}
