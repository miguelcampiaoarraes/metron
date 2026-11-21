package com.metron.repository;

import com.metron.model.entity.Income;
import com.metron.model.entity.ReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByUserIdOrderByReceiptDateDesc(Long userId);

    List<Income> findByUserIdAndReceiptDateBetweenOrderByReceiptDateDesc(Long userId, LocalDate start, LocalDate end);

    Optional<Income> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user.id = :userId AND i.receiptStatus = :status AND i.receiptDate BETWEEN :start AND :end")
    BigDecimal sumAmountByUserIdAndReceiptStatusAndReceiptDateBetween(
            @Param("userId") Long userId,
            @Param("status") ReceiptStatus status,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user.id = :userId AND i.receiptStatus = :status")
    BigDecimal sumAmountByUserIdAndReceiptStatus(
            @Param("userId") Long userId,
            @Param("status") ReceiptStatus status);
}
