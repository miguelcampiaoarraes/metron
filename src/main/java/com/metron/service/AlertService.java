package com.metron.service;

import com.metron.dto.DailyLimitDto;
import com.metron.model.entity.Alert;
import com.metron.model.entity.AlertType;
import com.metron.model.entity.FinancialGoal;
import com.metron.model.entity.GoalStatus;
import com.metron.model.entity.User;
import com.metron.repository.AlertRepository;
import com.metron.repository.FinancialGoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final FinancialGoalRepository goalRepository;
    private final FinancialEngineService engineService;

    public AlertService(AlertRepository alertRepository,
                        FinancialGoalRepository goalRepository,
                        FinancialEngineService engineService) {
        this.alertRepository = alertRepository;
        this.goalRepository = goalRepository;
        this.engineService = engineService;
    }

    public List<Alert> getUserAlerts(User user) {
        return alertRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public long getUnreadCount(User user) {
        return alertRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(User user, Long alertId) {
        alertRepository.findById(alertId).ifPresent(alert -> {
            if (alert.getUser().getId().equals(user.getId())) {
                alert.setRead(true);
                alertRepository.save(alert);
            }
        });
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Alert> unread = alertRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        for (Alert a : unread) {
            a.setRead(true);
        }
        alertRepository.saveAll(unread);
    }

    @Transactional
    public void generateSystemAlertsForUser(User user) {
        DailyLimitDto limitDto = engineService.calculateDailyLimit(user);

        // Check if daily limit exceeded or at risk
        if ("VERMELHO".equals(limitDto.getBudgetHealthStatus())) {
            createAlertIfNotRecent(user, "Atenção ao Limite Diário",
                    limitDto.getHealthMessage(), AlertType.DANGER);
        } else if ("AMARELO".equals(limitDto.getBudgetHealthStatus())) {
            createAlertIfNotRecent(user, "Limite Diário Próximo do Teto",
                    limitDto.getHealthMessage(), AlertType.WARNING);
        }

        // Check Goals near completion (>= 80%)
        List<FinancialGoal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        for (FinancialGoal g : goals) {
            if (g.getStatus() == GoalStatus.EM_ANDAMENTO && g.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal progress = g.getCurrentAmount().multiply(new BigDecimal("100"))
                        .divide(g.getTargetAmount(), 1, RoundingMode.HALF_UP);
                if (progress.compareTo(new BigDecimal("80")) >= 0 && progress.compareTo(new BigDecimal("100")) < 0) {
                    createAlertIfNotRecent(user, "Meta Financeira Quase Lá!",
                            String.format("Sua meta '%s' atingiu %.0f%% do valor desejado! Faltam apenas R$ %s.",
                                    g.getTitle(), progress.doubleValue(), g.getTargetAmount().subtract(g.getCurrentAmount()).setScale(2, RoundingMode.HALF_UP)),
                            AlertType.SUCCESS);
                } else if (progress.compareTo(new BigDecimal("100")) >= 0) {
                    createAlertIfNotRecent(user, "Meta Financeira Conquistada!",
                            String.format("Parabéns! Você alcançou a meta '%s' com R$ %s acumulados!",
                                    g.getTitle(), g.getCurrentAmount().setScale(2, RoundingMode.HALF_UP)),
                            AlertType.SUCCESS);
                }
            }
        }
    }

    private void createAlertIfNotRecent(User user, String title, String message, AlertType type) {
        List<Alert> existing = alertRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        boolean alreadyExists = existing.stream().anyMatch(a -> a.getTitle().equalsIgnoreCase(title));
        if (!alreadyExists) {
            Alert alert = new Alert(user, title, message, type);
            alertRepository.save(alert);
        }
    }
}
