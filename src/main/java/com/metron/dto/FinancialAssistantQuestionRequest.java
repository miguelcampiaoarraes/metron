package com.metron.dto;

import jakarta.validation.constraints.NotBlank;

public class FinancialAssistantQuestionRequest {

    @NotBlank(message = "A pergunta ou solicitação para o assistente não pode estar vazia.")
    private String question;

    public FinancialAssistantQuestionRequest() {}

    public FinancialAssistantQuestionRequest(String question) {
        this.question = question;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}
