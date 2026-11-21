package com.metron;

import com.metron.dto.DailyLimitDto;
import com.metron.dto.PurchaseSimulationRequest;
import com.metron.dto.PurchaseSimulationResponse;
import com.metron.model.entity.User;
import com.metron.repository.BudgetRepository;
import com.metron.repository.ExpenseRepository;
import com.metron.service.FinancialEngineService;
import com.metron.service.PurchaseSimulatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseSimulatorServiceTest {

    @Mock
    private FinancialEngineService engineService;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private PurchaseSimulatorService simulatorService;

    private User user;
    private DailyLimitDto dailyLimit;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Renata Lima");
        user.setMonthlyIncome(new BigDecimal("3000.00"));
        user.setCurrentBankBalance(new BigDecimal("2500.00"));
        user.setSavingsGoalMonthly(new BigDecimal("500.00"));

        dailyLimit = new DailyLimitDto();
        dailyLimit.setPlanningAvailableBalance(new BigDecimal("1200.00"));
        dailyLimit.setRecommendedDailyLimit(new BigDecimal("80.00"));
        dailyLimit.setRemainingToday(new BigDecimal("80.00"));
        dailyLimit.setDaysUntilNextSalary(15);
    }

    @Test
    @DisplayName("Simulação de Compra Aprovada dentro do limite diário")
    void testPurchaseWithinDailyLimit() {
        when(engineService.calculateDailyLimit(user)).thenReturn(dailyLimit);

        PurchaseSimulationRequest request = new PurchaseSimulationRequest("Almoço especial", new BigDecimal("50.00"));
        PurchaseSimulationResponse response = simulatorService.simulatePurchase(user, request);

        assertThat(response.isAffordable()).isTrue();
        assertThat(response.getVerdict()).isEqualTo("APROVADO");
        assertThat(response.getRemainingDailyAfterPurchase()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(response.getExcessOverDailyLimit()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Simulação de Compra que ultrapassa o limite diário mas cabe no saldo mensal (ATENÇÃO)")
    void testPurchaseExceedingDailyLimitPromptExample() {
        // Prompt section 11 example: "Quero gastar R$ 80 em uma compra. Posso?"
        // With daily limit R$ 50, excess is R$ 30
        dailyLimit.setRecommendedDailyLimit(new BigDecimal("50.00"));
        dailyLimit.setRemainingToday(new BigDecimal("50.00"));
        dailyLimit.setPlanningAvailableBalance(new BigDecimal("700.00"));
        when(engineService.calculateDailyLimit(user)).thenReturn(dailyLimit);

        PurchaseSimulationRequest request = new PurchaseSimulationRequest("Calçado novo", new BigDecimal("80.00"));
        PurchaseSimulationResponse response = simulatorService.simulatePurchase(user, request);

        assertThat(response.getVerdict()).isEqualTo("ATENCAO");
        assertThat(response.getExcessOverDailyLimit()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(response.getSummaryMessage()).contains("ultrapassaria seu limite diário recomendado em R$ 30,00");
    }

    @Test
    @DisplayName("Simulação de Compra Desaconselhada quando o valor excede todo o saldo livre")
    void testPurchaseExcessiveDesaconselhado() {
        dailyLimit.setPlanningAvailableBalance(new BigDecimal("100.00"));
        dailyLimit.setRemainingToday(new BigDecimal("20.00"));
        when(engineService.calculateDailyLimit(user)).thenReturn(dailyLimit);

        PurchaseSimulationRequest request = new PurchaseSimulationRequest("Smartphone novo", new BigDecimal("800.00"));
        PurchaseSimulationResponse response = simulatorService.simulatePurchase(user, request);

        assertThat(response.isAffordable()).isFalse();
        assertThat(response.getVerdict()).isEqualTo("DESACONSELHADO");
        assertThat(response.isSavingsCompromised()).isTrue();
    }
}
