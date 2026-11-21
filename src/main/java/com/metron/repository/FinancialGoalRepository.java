package com.metron.repository;

import com.metron.model.entity.FinancialGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Long> {

    List<FinancialGoal> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<FinancialGoal> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(g.currentAmount), 0) FROM FinancialGoal g WHERE g.user.id = :userId AND g.status = 'EM_ANDAMENTO'")
    BigDecimal sumCurrentAmountByUserId(@Param("userId") Long userId);
}
