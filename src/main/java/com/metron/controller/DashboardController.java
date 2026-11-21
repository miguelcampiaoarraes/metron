package com.metron.controller;

import com.metron.dto.DashboardSummaryDto;
import com.metron.model.entity.User;
import com.metron.security.CurrentUserContext;
import com.metron.service.FinancialEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final FinancialEngineService engineService;
    private final CurrentUserContext userContext;

    public DashboardController(FinancialEngineService engineService, CurrentUserContext userContext) {
        this.engineService = engineService;
        this.userContext = userContext;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary(@RequestParam(required = false, defaultValue = "mes") String period) {
        User user = userContext.getCurrentUser();
        return ResponseEntity.ok(engineService.getDashboardSummary(user, period));
    }
}
