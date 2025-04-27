package com.rh.cvfilter.llm;

import java.util.List;

/**
 * Interface définissant les opérations communes pour tous les fournisseurs de modèles de langage
 */
public interface LLMProvider {
    
    /**
     * Vérifie si une API key est configurée et disponible
     *
     * @return true si une clé API est configurée, false sinon
     */
    boolean isApiKeyConfigured();
    
    /**
     * Vérifie si l'API est accessible
     *
     * @return true si l'API est accessible, false sinon
     */
    boolean isApiAccessible();
    
    /**
     * Analyse un CV avec un LLM pour en extraire les informations pertinentes
     *
     * @param cvContent Le contenu du CV
     * @return Une analyse structurée du CV
     */
    CVAnalysisResult analyzeCVWithLLM(String cvContent);
    
    /**
     * Compare un CV avec une offre d'emploi pour déterminer la pertinence
     *
     * @param cvContent Le contenu du CV
     * @param jobOffer  La description du poste
     * @return Un score de pertinence et une explication
     */
    JobMatchResult evaluateCVForJobOffer(String cvContent, String jobOffer);
    
    /**
     * Génère des questions d'entretien personnalisées pour un candidat
     *
     * @param cvContent Le contenu du CV
     * @param jobOffer  La description du poste
     * @return Liste de questions d'entretien personnalisées
     */
    List<String> generateInterviewQuestions(String cvContent, String jobOffer);
} 