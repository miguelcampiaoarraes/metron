package com.metron.repository;

import com.metron.model.entity.Expense;
import com.metron.model.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserIdOrderByExpenseDateDesc(Long userId);

    List<Expense> findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(Long userId, LocalDate start, LocalDate end);

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.paymentStatus = :status AND e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumAmountByUserIdAndPaymentStatusAndExpenseDateBetween(
            @Param("userId") Long userId,
            @Param("status") PaymentStatus status,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.paymentStatus = :status")
    BigDecimal sumAmountByUserIdAndPaymentStatus(
            @Param("userId") Long userId,
            @Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.paymentStatus = 'PAGO' AND e.expenseDate = :date")
    BigDecimal sumPaidAmountByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT e.category.name, COALESCE(SUM(e.amount), 0), e.category.color " +
           "FROM Expense e WHERE e.user.id = :userId AND e.paymentStatus = 'PAGO' AND e.expenseDate BETWEEN :start AND :end " +
           "GROUP BY e.category.name, e.category.color ORDER BY SUM(e.amount) DESC")
    List<Object[]> findCategoryExpensesBetween(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.isRecurring = true")
    List<Expense> findRecurringExpenses(@Param("userId") Long userId);
}
