package com.metron.service;

import com.metron.dto.FinancialGoalDto;
import com.metron.exception.BusinessValidationException;
import com.metron.exception.ResourceNotFoundException;
import com.metron.model.entity.FinancialGoal;
import com.metron.model.entity.GoalStatus;
import com.metron.model.entity.User;
import com.metron.repository.FinancialGoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinancialGoalService {

    private final FinancialGoalRepository goalRepository;

    public FinancialGoalService(FinancialGoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public List<FinancialGoalDto> findAll(User user) {
        return goalRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::enrichGoalDto)
                .collect(Collectors.toList());
    }

    public FinancialGoalDto findById(User user, Long id) {
        FinancialGoal goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Meta financeira não encontrada ou acesso não autorizado."));
        return enrichGoalDto(goal);
    }

    @Transactional
    public FinancialGoalDto create(User user, FinancialGoalDto dto) {
        FinancialGoal goal = new FinancialGoal();
        goal.setUser(user);
        goal.setTitle(dto.getTitle().trim());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setCurrentAmount(dto.getCurrentAmount() != null ? dto.getCurrentAmount() : BigDecimal.ZERO);
        goal.setDeadline(dto.getDeadline());
        goal.setCategory(dto.getCategory());
        goal.setNotes(dto.getNotes());
        goal.setStatus(GoalStatus.EM_ANDAMENTO);

        FinancialGoal saved = goalRepository.save(goal);
        return enrichGoalDto(saved);
    }

    @Transactional
    public FinancialGoalDto update(User user, Long id, FinancialGoalDto dto) {
        FinancialGoal goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Meta financeira não encontrada ou acesso não autorizado."));

        goal.setTitle(dto.getTitle().trim());
        goal.setTargetAmount(dto.getTargetAmount());
        if (dto.getCurrentAmount() != null) {
            goal.setCurrentAmount(dto.getCurrentAmount());
        }
        goal.setDeadline(dto.getDeadline());
        goal.setCategory(dto.getCategory());
        goal.setNotes(dto.getNotes());
        if (dto.getStatus() != null) {
            goal.setStatus(dto.getStatus());
        }

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.CONCLUIDA);
        }

        FinancialGoal saved = goalRepository.save(goal);
        return enrichGoalDto(saved);
    }

    @Transactional
    public FinancialGoalDto deposit(User user, Long id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("O valor depositado na meta deve ser maior que zero.");
        }

        FinancialGoal goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Meta não encontrada."));

        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.CONCLUIDA);
        }

        FinancialGoal saved = goalRepository.save(goal);
        return enrichGoalDto(saved);
    }

    @Transactional
    public void delete(User user, Long id) {
        FinancialGoal goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Meta não encontrada ou acesso não autorizado."));
        goalRepository.delete(goal);
    }

    private FinancialGoalDto enrichGoalDto(FinancialGoal goal) {
        FinancialGoalDto dto = new FinancialGoalDto(goal);

        BigDecimal target = goal.getTargetAmount();
        BigDecimal current = goal.getCurrentAmount();

        if (target != null && target.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal progress = current.multiply(new BigDecimal("100"))
                    .divide(target, 1, RoundingMode.HALF_UP);
            dto.setProgressPercentage(progress.min(new BigDecimal("100.0")));

            BigDecimal remaining = target.subtract(current);
            dto.setRemainingAmount(remaining.max(BigDecimal.ZERO));

            // Monthly savings needed
            if (goal.getDeadline() != null && goal.getDeadline().isAfter(LocalDate.now())) {
                Period period = Period.between(LocalDate.now(), goal.getDeadline());
                int months = period.getYears() * 12 + period.getMonths();
                if (period.getDays() > 0) {
                    months += 1;
                }
                if (months <= 0) months = 1;

                BigDecimal needed = dto.getRemainingAmount().divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
                dto.setMonthlySavingsNeeded(needed);
            } else {
                dto.setMonthlySavingsNeeded(dto.getRemainingAmount());
            }
        }

        return dto;
    }
}
