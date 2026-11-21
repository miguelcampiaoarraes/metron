package com.metron.controller;

import com.metron.dto.FinancialAssistantQuestionRequest;
import com.metron.dto.FinancialAssistantResponse;
import com.metron.model.entity.User;
import com.metron.security.CurrentUserContext;
import com.metron.service.FinancialAssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final FinancialAssistantService assistantService;
    private final CurrentUserContext userContext;

    public AssistantController(FinancialAssistantService assistantService, CurrentUserContext userContext) {
        this.assistantService = assistantService;
        this.userContext = userContext;
    }

    @PostMapping("/ask")
    public ResponseEntity<FinancialAssistantResponse> askQuestion(@Valid @RequestBody FinancialAssistantQuestionRequest request) {
        User user = userContext.getCurrentUser();
        return ResponseEntity.ok(assistantService.answer(user, request.getQuestion()));
    }
}
