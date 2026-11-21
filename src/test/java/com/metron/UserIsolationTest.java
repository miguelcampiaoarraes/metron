package com.metron;

import com.metron.dto.ExpenseRequest;
import com.metron.dto.IncomeRequest;
import com.metron.exception.ResourceNotFoundException;
import com.metron.model.entity.*;
import com.metron.repository.CategoryRepository;
import com.metron.repository.ExpenseRepository;
import com.metron.repository.IncomeRepository;
import com.metron.service.ExpenseService;
import com.metron.service.IncomeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserIsolationTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @InjectMocks
    private IncomeService incomeService;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = new User();
        userA.setId(10L);
        userA.setEmail("usera@metron.com");

        userB = new User();
        userB.setId(20L);
        userB.setEmail("userb@metron.com");
    }

    @Test
    @DisplayName("Usuário B não pode consultar despesa pertencente ao Usuário A")
    void userBCannotReadUserAExpense() {
        when(expenseRepository.findByIdAndUserId(100L, userB.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.findById(userB, 100L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Despesa não encontrada ou acesso não autorizado");

        verify(expenseRepository).findByIdAndUserId(100L, userB.getId());
    }

    @Test
    @DisplayName("Usuário B não pode atualizar despesa pertencente ao Usuário A")
    void userBCannotUpdateUserAExpense() {
        when(expenseRepository.findByIdAndUserId(100L, userB.getId()))
                .thenReturn(Optional.empty());

        ExpenseRequest updateReq = new ExpenseRequest();
        updateReq.setDescription("Fraude");
        updateReq.setAmount(new BigDecimal("999.00"));
        updateReq.setExpenseDate(LocalDate.now());
        updateReq.setPaymentMethod(PaymentMethod.PIX);
        updateReq.setPaymentStatus(PaymentStatus.PAGO);

        assertThatThrownBy(() -> expenseService.update(userB, 100L, updateReq))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Usuário B não pode excluir despesa pertencente ao Usuário A")
    void userBCannotDeleteUserAExpense() {
        when(expenseRepository.findByIdAndUserId(100L, userB.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.delete(userB, 100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Usuário B não pode consultar receita pertencente ao Usuário A")
    void userBCannotReadUserAIncome() {
        when(incomeRepository.findByIdAndUserId(200L, userB.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.findById(userB, 200L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Receita não encontrada ou acesso não autorizado");
    }

    @Test
    @DisplayName("Usuário B não pode atualizar receita pertencente ao Usuário A")
    void userBCannotUpdateUserAIncome() {
        when(incomeRepository.findByIdAndUserId(200L, userB.getId()))
                .thenReturn(Optional.empty());

        IncomeRequest req = new IncomeRequest();
        req.setDescription("Receita maliciosa");
        req.setAmount(new BigDecimal("500.00"));
        req.setReceiptDate(LocalDate.now());
        req.setIncomeType(IncomeType.SALARIO);
        req.setReceiptStatus(ReceiptStatus.RECEBIDO);

        assertThatThrownBy(() -> incomeService.update(userB, 200L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
