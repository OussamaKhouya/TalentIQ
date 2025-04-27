package com.rh.cvfilter.model;

import java.util.ArrayList;
import java.util.List;

public class CVInfo {
    private String name;
    private String email;
    private String phone;
    private String downloadId;
    private int score;
    private String matchExplanation;
    private List<String> skills = new ArrayList<>();
    private String experience;
    private String education;
    private List<String> languages = new ArrayList<>();
    private List<String> interviewQuestions = new ArrayList<>();

    public CVInfo(String name, String email, String phone, String downloadId) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.downloadId = downloadId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(String downloadId) {
        this.downloadId = downloadId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getMatchExplanation() {
        return matchExplanation;
    }

    public void setMatchExplanation(String matchExplanation) {
        this.matchExplanation = matchExplanation;
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

    public List<String> getInterviewQuestions() {
        return interviewQuestions;
    }

    public void setInterviewQuestions(List<String> interviewQuestions) {
        this.interviewQuestions = interviewQuestions;
    }

    @Override
    public String toString() {
        return "CVInfo{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", downloadId='" + downloadId + '\'' +
                ", score=" + score +
                ", skills=" + skills +
                ", experience='" + experience + '\'' +
                ", education='" + education + '\'' +
                ", languages=" + languages +
                '}';
    }
} 