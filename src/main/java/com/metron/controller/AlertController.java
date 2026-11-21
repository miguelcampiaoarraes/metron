package com.metron.controller;

import com.metron.model.entity.Alert;
import com.metron.model.entity.User;
import com.metron.security.CurrentUserContext;
import com.metron.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;
    private final CurrentUserContext userContext;

    public AlertController(AlertService alertService, CurrentUserContext userContext) {
        this.alertService = alertService;
        this.userContext = userContext;
    }

    @GetMapping
    public ResponseEntity<List<Alert>> getAlerts() {
        User user = userContext.getCurrentUser();
        alertService.generateSystemAlertsForUser(user);
        return ResponseEntity.ok(alertService.getUserAlerts(user));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        User user = userContext.getCurrentUser();
        long count = alertService.getUnreadCount(user);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        User user = userContext.getCurrentUser();
        alertService.markAsRead(user, id);
        return ResponseEntity.ok(Map.of("message", "Alerta marcado como lido."));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        User user = userContext.getCurrentUser();
        alertService.markAllAsRead(user);
        return ResponseEntity.ok(Map.of("message", "Todos os alertas foram marcados como lidos."));
    }
}
