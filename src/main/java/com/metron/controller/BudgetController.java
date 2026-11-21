package com.metron.controller;

import com.metron.dto.BudgetDto;
import com.metron.model.entity.User;
import com.metron.security.CurrentUserContext;
import com.metron.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final CurrentUserContext userContext;

    public BudgetController(BudgetService budgetService, CurrentUserContext userContext) {
        this.budgetService = budgetService;
        this.userContext = userContext;
    }

    @GetMapping("/{year}/{month}")
    public ResponseEntity<BudgetDto> getBudget(@PathVariable Integer year, @PathVariable Integer month) {
        User user = userContext.getCurrentUser();
        return ResponseEntity.ok(budgetService.getBudget(user, month, year));
    }

    @PostMapping
    public ResponseEntity<BudgetDto> saveOrUpdate(@Valid @RequestBody BudgetDto dto) {
        User user = userContext.getCurrentUser();
        return ResponseEntity.ok(budgetService.saveOrUpdate(user, dto));
    }
}
