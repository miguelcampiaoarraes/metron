package com.metron.controller;

import com.metron.dto.FinancialGoalDto;
import com.metron.model.entity.User;
import com.metron.security.CurrentUserContext;
import com.metron.service.FinancialGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class FinancialGoalController {

    private final FinancialGoalService goalService;
    private final CurrentUserContext userContext;

    public FinancialGoalController(FinancialGoalService goalService, CurrentUserContext userContext) {
        this.goalService = goalService;
        this.userContext = userContext;
    }

    @GetMapping
    public ResponseEntity<List<FinancialGoalDto>> getGoals() {
        User user = userContext.getCurrentUser();
        return ResponseEntity.ok(goalService.findAll(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinancialGoalDto> getGoalById(@PathVariable Long id) {
        User user = userContext.getCurrentUser();
        return ResponseEntity.ok(goalService.findById(user, id));
    }

    @PostMapping
    public ResponseEntity<FinancialGoalDto> createGoal(@Valid @RequestBody FinancialGoalDto dto) {
        User user = userContext.getCurrentUser();
        FinancialGoalDto created = goalService.create(user, dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FinancialGoalDto> updateGoal(@PathVariable Long id, @Valid @RequestBody FinancialGoalDto dto) {
        User user = userContext.getCurrentUser();
        FinancialGoalDto updated = goalService.update(user, id, dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<FinancialGoalDto> deposit(@PathVariable Long id, @RequestBody Map<String, BigDecimal> payload) {
        User user = userContext.getCurrentUser();
        BigDecimal amount = payload.get("amount");
        FinancialGoalDto updated = goalService.deposit(user, id, amount);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(@PathVariable Long id) {
        User user = userContext.getCurrentUser();
        goalService.delete(user, id);
        return ResponseEntity.ok(Map.of("message", "Meta excluída com sucesso."));
    }
}
