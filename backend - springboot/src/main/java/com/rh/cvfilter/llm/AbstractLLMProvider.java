package com.rh.cvfilter.llm;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe abstraite fournissant des implémentations communes pour les fournisseurs de LLM
 */
public abstract class AbstractLLMProvider implements LLMProvider {

    /**
     * Méthode de secours pour générer une analyse de CV basique
     */
    protected CVAnalysisResult generateFallbackCVAnalysis(String cvContent) {
        CVAnalysisResult result = new CVAnalysisResult();

        // Extraire des compétences basiques en cherchant des mots techniques
        List<String> skills = extractSkillsFromCV(cvContent);
        result.setSkills(skills);

        // Extraire expérience (lignes contenant "expérience" ou "travail")
        String experience = extractExperience(cvContent);
        result.setExperience(experience);

        // Extraire formation (lignes contenant "formation", "éducation", "diplôme")
        String education = extractEducation(cvContent);
        result.setEducation(education);

        // Extraire langues (lignes contenant "langue", "anglais", "français", etc.)
        List<String> languages = extractLanguages(cvContent);
        result.setLanguages(languages);

        return result;
    }

    /**
     * Méthode de secours pour générer un score de correspondance basique
     */
    protected JobMatchResult generateFallbackJobMatch(String cvContent, String jobOffer) {
        // Calculer un score basique basé sur le nombre de mots communs
        String[] jobWords = jobOffer.toLowerCase().split("\\W+");
        String[] cvWords = cvContent.toLowerCase().split("\\W+");

        int matches = 0;
        for (String jobWord : jobWords) {
            if (jobWord.length() <= 3) continue; // Ignorer les mots courts

            for (String cvWord : cvWords) {
                if (cvWord.equals(jobWord)) {
                    matches++;
                    break;
                }
            }
        }

        int score = Math.min(90, Math.max(30, (matches * 100) / Math.max(1, jobWords.length)));

        // Générer une explication basique
        String explanation = "Score basé sur l'analyse des correspondances de mots-clés entre le CV et l'offre d'emploi. " +
                "Le candidat a " + matches + " correspondances de mots-clés avec l'offre.";

        return new JobMatchResult(score, explanation);
    }

    /**
     * Méthode de secours pour générer des questions d'entretien basiques
     */
    protected List<String> generateFallbackInterviewQuestions(String cvContent) {
        List<String> questions = new ArrayList<>();
        questions.add("Pouvez-vous me parler de votre expérience professionnelle la plus récente ?");
        questions.add("Quelles sont vos compétences techniques principales ?");
        questions.add("Comment avez-vous résolu un problème technique complexe dans votre dernier poste ?");
        questions.add("Pourquoi êtes-vous intéressé par ce poste et notre entreprise ?");
        questions.add("Avez-vous des questions à nous poser sur le poste ou l'entreprise ?");
        return questions;
    }

    /**
     * Extraire des compétences du CV en mode de secours
     */
    protected List<String> extractSkillsFromCV(String cvContent) {
        List<String> skills = new ArrayList<>();
        // Liste de compétences techniques communes à rechercher
        String[] commonSkills = {"Java", "Python", "JavaScript", "HTML", "CSS", "SQL", "NoSQL", "Angular",
                "React", "Vue", "Node.js", "Spring", "Hibernate", "JPA", "AWS", "Azure",
                "Docker", "Kubernetes", "DevOps", "CI/CD", "Git", "Agile", "Scrum",
                "PHP", "C++", "C#", ".NET", "Ruby", "Swift", "Kotlin", "Android", "iOS",
                "Linux", "Windows", "MacOS", "REST", "API", "JSON", "XML", "SOAP",
                "MongoDB", "PostgreSQL", "MySQL", "Oracle", "Redis", "ElasticSearch"};

        for (String skill : commonSkills) {
            if (cvContent.toLowerCase().contains(skill.toLowerCase())) {
                skills.add(skill);
            }
        }

        return skills;
    }

    /**
     * Extraire l'expérience du CV en mode de secours
     */
    protected String extractExperience(String cvContent) {
        StringBuilder experience = new StringBuilder();
        String[] lines = cvContent.split("\n");
        boolean inExperienceSection = false;

        for (String line : lines) {
            if (line.toLowerCase().contains("expérience") ||
                    line.toLowerCase().contains("travail") ||
                    line.toLowerCase().contains("emploi") ||
                    line.toLowerCase().contains("professionnelle")) {
                inExperienceSection = true;
                experience.append(line).append("\n");
            } else if (inExperienceSection) {
                if (line.toLowerCase().contains("formation") ||
                        line.toLowerCase().contains("éducation") ||
                        line.toLowerCase().contains("compétence") ||
                        line.isEmpty()) {
                    inExperienceSection = false;
                } else {
                    experience.append(line).append("\n");
                }
            }
        }

        return experience.toString().trim();
    }

    /**
     * Extraire la formation du CV en mode de secours
     */
    protected String extractEducation(String cvContent) {
        StringBuilder education = new StringBuilder();
        String[] lines = cvContent.split("\n");
        boolean inEducationSection = false;

        for (String line : lines) {
            if (line.toLowerCase().contains("formation") ||
                    line.toLowerCase().contains("éducation") ||
                    line.toLowerCase().contains("diplôme") ||
                    line.toLowerCase().contains("études")) {
                inEducationSection = true;
                education.append(line).append("\n");
            } else if (inEducationSection) {
                if (line.toLowerCase().contains("expérience") ||
                        line.toLowerCase().contains("compétence") ||
                        line.toLowerCase().contains("langue") ||
                        line.isEmpty()) {
                    inEducationSection = false;
                } else {
                    education.append(line).append("\n");
                }
            }
        }

        return education.toString().trim();
    }

    /**
     * Extraire les langues du CV en mode de secours
     */
    protected List<String> extractLanguages(String cvContent) {
        List<String> languages = new ArrayList<>();
        String[] commonLanguages = {"français", "anglais", "espagnol", "allemand", "italien", "chinois",
                "arabe", "russe", "portugais", "japonais"};

        for (String language : commonLanguages) {
            if (cvContent.toLowerCase().contains(language.toLowerCase())) {
                languages.add(language.substring(0, 1).toUpperCase() + language.substring(1));
            }
        }

        return languages;
    }

    /**
     * Parse la réponse du LLM en un objet CVAnalysisResult
     */
    protected CVAnalysisResult parseCVAnalysisResult(String llmResponse) {
        CVAnalysisResult result = new CVAnalysisResult();

        // Analyse simple de la réponse textuelle
        String[] lines = llmResponse.split("\n");
        List<String> skills = new ArrayList<>();
        StringBuilder experience = new StringBuilder();
        StringBuilder education = new StringBuilder();
        List<String> languages = new ArrayList<>();

        int section = 0;
        for (String line : lines) {
            line = line.trim();
            if (line.contains("Compétences") || line.contains("compétences") || line.matches(".*1\\..*")) {
                section = 1;
                continue;
            } else if (line.contains("Expérience") || line.contains("expérience") || line.matches(".*2\\..*")) {
                section = 2;
                continue;
            } else if (line.contains("Formation") || line.contains("formation") || line.matches(".*3\\..*")) {
                section = 3;
                continue;
            } else if (line.contains("Langues") || line.contains("langues") || line.matches(".*4\\..*")) {
                section = 4;
                continue;
            }

            if (line.isEmpty()) continue;

            switch (section) {
                case 1:
                    // Compétences
                    if (line.contains("-") || line.contains("•")) {
                        skills.add(line.replaceAll("^[\\-•\\s]+", "").trim());
                    } else {
                        String[] skillTokens = line.split(",|;|\\s+et\\s+");
                        for (String skill : skillTokens) {
                            String cleanSkill = skill.trim();
                            if (!cleanSkill.isEmpty()) {
                                skills.add(cleanSkill);
                            }
                        }
                    }
                    break;
                case 2:
                    // Expérience
                    experience.append(line).append("\n");
                    break;
                case 3:
                    // Formation
                    education.append(line).append("\n");
                    break;
                case 4:
                    // Langues
                    if (line.contains("-") || line.contains("•")) {
                        languages.add(line.replaceAll("^[\\-•\\s]+", "").trim());
                    } else {
                        String[] languageTokens = line.split(",|;|\\s+et\\s+");
                        for (String language : languageTokens) {
                            String cleanLanguage = language.trim();
                            if (!cleanLanguage.isEmpty()) {
                                languages.add(cleanLanguage);
                            }
                        }
                    }
                    break;
            }
        }

        result.setSkills(skills);
        result.setExperience(experience.toString().trim());
        result.setEducation(education.toString().trim());
        result.setLanguages(languages);

        return result;
    }

    /**
     * Parse la réponse du LLM en un objet JobMatchResult
     */
    protected JobMatchResult parseJobMatchResult(String llmResponse) {
        // Recherche d'un score dans la réponse
        int score = 0;
        String explanation = llmResponse.trim();

        // Recherche d'un score numérique dans la réponse
        String[] lines = llmResponse.split("\n");
        for (String line : lines) {
            if (line.contains("Score") || line.contains("score") || line.contains("Note") || line.contains("note")) {
                String[] parts = line.split(":");
                if (parts.length > 1) {
                    String scorePart = parts[1].trim();
                    // Extraire un nombre de 0 à 100
                    try {
                        score = Integer.parseInt(scorePart.replaceAll("[^0-9]", ""));
                        if (score > 100) score = score % 100;
                        break;
                    } catch (NumberFormatException e) {
                        // Continuer à chercher
                    }
                }
            }
        }

        return new JobMatchResult(score, explanation);
    }

    /**
     * Parse la réponse du LLM en une liste de questions d'entretien
     */
    protected List<String> parseInterviewQuestions(String llmResponse) {
        List<String> questions = new ArrayList<>();

        // Analyse simple de la réponse pour extraire les questions
        String[] lines = llmResponse.split("\n");
        for (String line : lines) {
            line = line.trim();

            // Ignorer les lignes vides et les titres
            if (line.isEmpty() || line.contains("Questions") || line.contains("questions")) {
                continue;
            }

            // Détecter les questions (commençant par un nombre ou contenant un point d'interrogation)
            if (line.matches("^\\d+\\..*") || line.contains("?")) {
                // Nettoyer la question (enlever les numéros au début)
                String question = line.replaceAll("^\\d+\\.\\s*", "").trim();
                if (!question.isEmpty()) {
                    questions.add(question);
                }
            }
        }

        return questions;
    }
} 