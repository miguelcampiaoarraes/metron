package com.metron.controller;

import com.metron.dto.DailyLimitDto;
import com.metron.model.entity.User;
import com.metron.security.CurrentUserContext;
import com.metron.service.FinancialEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/daily-limit")
public class DailyLimitController {

    private final FinancialEngineService engineService;
    private final CurrentUserContext userContext;

    public DailyLimitController(FinancialEngineService engineService, CurrentUserContext userContext) {
        this.engineService = engineService;
        this.userContext = userContext;
    }

    @GetMapping
    public ResponseEntity<DailyLimitDto> getDailyLimit() {
        User user = userContext.getCurrentUser();
        return ResponseEntity.ok(engineService.calculateDailyLimit(user));
    }
}
