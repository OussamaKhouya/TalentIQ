package com.rh.cvfilter.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service principal pour l'interaction avec les modèles de langage.
 * Fait office de façade et délègue aux différentes implémentations.
 */
@Service
public class LLMService {
    private static final Logger logger = LoggerFactory.getLogger(LLMService.class);

    @Autowired
    private GeminiLLMService geminiService;
    
    @Autowired
    private OllamaLLMService ollamaService;
    
    @Value("${llm.provider:gemini}")
    private String llmProvider;
    
    /**
     * Retourne le provider actif
     * @return Le nom du provider actif
     */
    public String getActiveProvider() {
        return llmProvider;
    }
    
    /**
     * Modifie le provider actif en cours d'exécution
     * @param provider Le nom du nouveau provider
     */
    public void setActiveProvider(String provider) {
        if (!"gemini".equals(provider) && !"ollama".equals(provider)) {
            throw new IllegalArgumentException("Provider non reconnu: " + provider);
        }
        
        logger.info("Changement du provider LLM de '{}' vers '{}'", this.llmProvider, provider);
        this.llmProvider = provider;
    }
    
    /**
     * Récupère le service de LLM approprié en fonction de la configuration
     * @return Le provider de LLM à utiliser
     */
    private LLMProvider getProvider() {
        if ("ollama".equalsIgnoreCase(llmProvider)) {
            logger.info("Utilisation du modèle local Ollama");
            return ollamaService;
        } else {
            logger.info("Utilisation de l'API Gemini");
            return geminiService;
        }
    }

    /**
     * Vérifie si une clé API est configurée et disponible
     *
     * @return true si une clé API est configurée, false sinon
     */
    public boolean isApiKeyConfigured() {
        return getProvider().isApiKeyConfigured();
    }

    /**
     * Vérifie si l'API est accessible
     *
     * @return true si l'API est accessible, false sinon
     */
    public boolean isApiAccessible() {
        return getProvider().isApiAccessible();
    }

    /**
     * Analyse un CV avec un LLM pour en extraire les informations pertinentes
     *
     * @param cvContent Le contenu du CV
     * @return Une analyse structurée du CV
     */
    public CVAnalysisResult analyzeCVWithLLM(String cvContent) {
        return getProvider().analyzeCVWithLLM(cvContent);
    }

    /**
     * Compare un CV avec une offre d'emploi pour déterminer la pertinence
     *
     * @param cvContent Le contenu du CV
     * @param jobOffer  La description du poste
     * @return Un score de pertinence et une explication
     */
    public JobMatchResult evaluateCVForJobOffer(String cvContent, String jobOffer) {
        return getProvider().evaluateCVForJobOffer(cvContent, jobOffer);
    }

    /**
     * Génère des questions d'entretien personnalisées pour un candidat
     *
     * @param cvContent Le contenu du CV
     * @param jobOffer  La description du poste
     * @return Liste de questions d'entretien personnalisées
     */
    public List<String> generateInterviewQuestions(String cvContent, String jobOffer) {
        return getProvider().generateInterviewQuestions(cvContent, jobOffer);
    }
} 