package com.rh.cvfilter.llm;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant le résultat d'une analyse de CV par LLM
 */
public class CVAnalysisResult {
    private List<String> skills = new ArrayList<>();
    private String experience = "";
    private String education = "";
    private List<String> languages = new ArrayList<>();

    public CVAnalysisResult() {
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    @Override
    public String toString() {
        return "CVAnalysisResult{" +
                "skills=" + skills +
                ", experience='" + experience + '\'' +
                ", education='" + education + '\'' +
                ", languages=" + languages +
                '}';
    }
} 