package com.metron.controller;

import com.metron.model.entity.User;
import com.metron.security.CurrentUserContext;
import com.metron.service.ExpensePatternService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patterns")
public class ExpensePatternController {

    private final ExpensePatternService patternService;
    private final CurrentUserContext userContext;

    public ExpensePatternController(ExpensePatternService patternService, CurrentUserContext userContext) {
        this.patternService = patternService;
        this.userContext = userContext;
    }

    @GetMapping("/insights")
    public ResponseEntity<List<ExpensePatternService.PatternInsight>> getInsights() {
        User user = userContext.getCurrentUser();
        return ResponseEntity.ok(patternService.analyzePatterns(user));
    }
}
