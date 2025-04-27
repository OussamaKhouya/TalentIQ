package com.rh.cvfilter.llm;

/**
 * Classe représentant le résultat d'une évaluation de correspondance CV/offre d'emploi
 */
public class JobMatchResult {
    private int score;
    private String explanation;

    public JobMatchResult(int score, String explanation) {
        this.score = score;
        this.explanation = explanation;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    @Override
    public String toString() {
        return "JobMatchResult{" +
                "score=" + score +
                ", explanation='" + explanation + '\'' +
                '}';
    }
} 