package com.metron.controller;

import com.metron.dto.ExpenseRequest;
import com.metron.dto.ExpenseResponse;
import com.metron.model.entity.User;
import com.metron.security.CurrentUserContext;
import com.metron.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CurrentUserContext userContext;

    public ExpenseController(ExpenseService expenseService, CurrentUserContext userContext) {
        this.expenseService = expenseService;
        this.userContext = userContext;
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getExpenses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "date_desc") String sortBy) {

        User user = userContext.getCurrentUser();
        List<ExpenseResponse> list = expenseService.findExpenses(user, startDate, endDate, categoryId, search, sortBy);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        User user = userContext.getCurrentUser();
        return ResponseEntity.ok(expenseService.findById(user, id));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        User user = userContext.getCurrentUser();
        ExpenseResponse created = expenseService.create(user, request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        User user = userContext.getCurrentUser();
        ExpenseResponse updated = expenseService.update(user, id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteExpense(@PathVariable Long id) {
        User user = userContext.getCurrentUser();
        expenseService.delete(user, id);
        return ResponseEntity.ok(Map.of("message", "Despesa excluída com sucesso."));
    }
}
