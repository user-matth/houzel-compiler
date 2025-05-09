package br.com.houzelcompiler.houzelcompiler.model;

public class EvaluationResponse {

    private String competencyEvaluation;
    private String finalScore;
    private String aiDetection;

    public EvaluationResponse() {}

    public EvaluationResponse(String competencyEvaluation, String finalScore, String aiDetection) {
        this.competencyEvaluation = competencyEvaluation;
        this.finalScore = finalScore;
        this.aiDetection = aiDetection;
    }

    public String getCompetencyEvaluation() {
        return competencyEvaluation;
    }

    public void setCompetencyEvaluation(String competencyEvaluation) {
        this.competencyEvaluation = competencyEvaluation;
    }

    public String getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(String finalScore) {
        this.finalScore = finalScore;
    }

    public String getAiDetection() {
        return aiDetection;
    }

    public void setAiDetection(String aiDetection) {
        this.aiDetection = aiDetection;
    }
}
