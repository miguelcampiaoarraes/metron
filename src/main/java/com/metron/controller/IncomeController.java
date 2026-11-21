package com.metron.controller;

import com.metron.dto.IncomeRequest;
import com.metron.dto.IncomeResponse;
import com.metron.model.entity.User;
import com.metron.security.CurrentUserContext;
import com.metron.service.IncomeService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incomes")
public class IncomeController {

    private final IncomeService incomeService;
    private final CurrentUserContext userContext;

    public IncomeController(IncomeService incomeService, CurrentUserContext userContext) {
        this.incomeService = incomeService;
        this.userContext = userContext;
    }

    @GetMapping
    public ResponseEntity<List<IncomeResponse>> getIncomes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "date_desc") String sortBy) {

        User user = userContext.getCurrentUser();
        List<IncomeResponse> list = incomeService.findIncomes(user, startDate, endDate, categoryId, search, sortBy);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> getIncomeById(@PathVariable Long id) {
        User user = userContext.getCurrentUser();
        return ResponseEntity.ok(incomeService.findById(user, id));
    }

    @PostMapping
    public ResponseEntity<IncomeResponse> createIncome(@Valid @RequestBody IncomeRequest request) {
        User user = userContext.getCurrentUser();
        IncomeResponse created = incomeService.create(user, request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponse> updateIncome(@PathVariable Long id, @Valid @RequestBody IncomeRequest request) {
        User user = userContext.getCurrentUser();
        IncomeResponse updated = incomeService.update(user, id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteIncome(@PathVariable Long id) {
        User user = userContext.getCurrentUser();
        incomeService.delete(user, id);
        return ResponseEntity.ok(Map.of("message", "Receita excluída com sucesso."));
    }
}
