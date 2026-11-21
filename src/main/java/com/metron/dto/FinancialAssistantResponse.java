package com.metron.dto;

public class FinancialAssistantResponse {

    private String question;
    private String situationAnalysis;
    private String usedData;
    private String calculationsPerformed;
    private String recommendation;
    private String estimatedSavings;
    private String suggestedNextAction;
    private boolean aiEnhanced = false;

    public FinancialAssistantResponse() {}

    public FinancialAssistantResponse(String question, String situationAnalysis, String usedData,
                                      String calculationsPerformed, String recommendation,
                                      String estimatedSavings, String suggestedNextAction, boolean aiEnhanced) {
        this.question = question;
        this.situationAnalysis = situationAnalysis;
        this.usedData = usedData;
        this.calculationsPerformed = calculationsPerformed;
        this.recommendation = recommendation;
        this.estimatedSavings = estimatedSavings;
        this.suggestedNextAction = suggestedNextAction;
        this.aiEnhanced = aiEnhanced;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getSituationAnalysis() { return situationAnalysis; }
    public void setSituationAnalysis(String situationAnalysis) { this.situationAnalysis = situationAnalysis; }

    public String getUsedData() { return usedData; }
    public void setUsedData(String usedData) { this.usedData = usedData; }

    public String getCalculationsPerformed() { return calculationsPerformed; }
    public void setCalculationsPerformed(String calculationsPerformed) { this.calculationsPerformed = calculationsPerformed; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public String getEstimatedSavings() { return estimatedSavings; }
    public void setEstimatedSavings(String estimatedSavings) { this.estimatedSavings = estimatedSavings; }

    public String getSuggestedNextAction() { return suggestedNextAction; }
    public void setSuggestedNextAction(String suggestedNextAction) { this.suggestedNextAction = suggestedNextAction; }

    public boolean isAiEnhanced() { return aiEnhanced; }
    public void setAiEnhanced(boolean aiEnhanced) { this.aiEnhanced = aiEnhanced; }
}
