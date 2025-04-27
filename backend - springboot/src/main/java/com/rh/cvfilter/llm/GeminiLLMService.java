package com.rh.cvfilter.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Service pour communiquer avec l'API Gemini de Google
 */
@Service
public class GeminiLLMService extends AbstractLLMProvider {
    private static final Logger logger = LoggerFactory.getLogger(GeminiLLMService.class);
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private boolean apiAccessible = true;

    @Value("${gemini.api.key:AIzaSyArGburS1cEGdgZpaI9yrNMFVqDgmWqyq4}")
    private String apiKey;

    /**
     * Vérifie si une clé API est configurée et disponible
     *
     * @return true si une clé API est configurée, false sinon
     */
    @Override
    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("''");
    }

    /**
     * Vérifie si l'API est accessible
     *
     * @return true si l'API est accessible, false sinon
     */
    @Override
    public boolean isApiAccessible() {
        return apiAccessible;
    }

    /**
     * Analyse un CV avec un LLM pour en extraire les informations pertinentes
     *
     * @param cvContent Le contenu du CV
     * @return Une analyse structurée du CV
     */
    @Override
    public CVAnalysisResult analyzeCVWithLLM(String cvContent) {
        try {
            if (!apiAccessible) {
                return generateFallbackCVAnalysis(cvContent);
            }

            String prompt = "Analyse ce CV et extrait les informations suivantes :\n" +
                    "1. Compétences techniques principales (liste de mots-clés)\n" +
                    "2. Expérience professionnelle (résumé)\n" +
                    "3. Formation (résumé)\n" +
                    "4. Langues parlées\n\n" +
                    "CV: " + cvContent.substring(0, Math.min(cvContent.length(), 3000));

            String llmResponse = askQuestion(prompt);

            if (llmResponse.isEmpty()) {
                apiAccessible = false;
                return generateFallbackCVAnalysis(cvContent);
            }

            return parseCVAnalysisResult(llmResponse);
        } catch (Exception e) {
            logger.error("Erreur lors de l'analyse du CV avec LLM", e);
            apiAccessible = false;
            return generateFallbackCVAnalysis(cvContent);
        }
    }

    /**
     * Compare un CV avec une offre d'emploi pour déterminer la pertinence
     *
     * @param cvContent Le contenu du CV
     * @param jobOffer  La description du poste
     * @return Un score de pertinence et une explication
     */
    @Override
    public JobMatchResult evaluateCVForJobOffer(String cvContent, String jobOffer) {
        try {
            if (!apiAccessible) {
                return generateFallbackJobMatch(cvContent, jobOffer);
            }

            String prompt = "Évalue la pertinence de ce CV pour cette offre d'emploi. " +
                    "Donne un score de pertinence de 0 à 100 et une explication détaillée des forces et faiblesses du candidat.\n\n" +
                    "CV: " + cvContent.substring(0, Math.min(cvContent.length(), 2000)) + "\n\n" +
                    "Offre d'emploi: " + jobOffer;

            String llmResponse = askQuestion(prompt);

            if (llmResponse.isEmpty()) {
                apiAccessible = false;
                return generateFallbackJobMatch(cvContent, jobOffer);
            }

            return parseJobMatchResult(llmResponse);
        } catch (Exception e) {
            logger.error("Erreur lors de l'évaluation du CV pour l'offre d'emploi", e);
            apiAccessible = false;
            return generateFallbackJobMatch(cvContent, jobOffer);
        }
    }

    /**
     * Génère des questions d'entretien personnalisées pour un candidat
     *
     * @param cvContent Le contenu du CV
     * @param jobOffer  La description du poste
     * @return Liste de questions d'entretien personnalisées
     */
    @Override
    public List<String> generateInterviewQuestions(String cvContent, String jobOffer) {
        try {
            if (!apiAccessible) {
                return generateFallbackInterviewQuestions(cvContent);
            }

            String prompt = "Génère 5 questions d'entretien pertinentes et techniques pour ce candidat " +
                    "en fonction de son CV et de l'offre d'emploi. Les questions doivent permettre d'évaluer " +
                    "les compétences du candidat par rapport aux exigences du poste.\n\n" +
                    "CV: " + cvContent.substring(0, Math.min(cvContent.length(), 1500)) + "\n\n" +
                    "Offre d'emploi: " + jobOffer;

            String llmResponse = askQuestion(prompt);

            if (llmResponse.isEmpty()) {
                apiAccessible = false;
                return generateFallbackInterviewQuestions(cvContent);
            }

            return parseInterviewQuestions(llmResponse);
        } catch (Exception e) {
            logger.error("Erreur lors de la génération des questions d'entretien", e);
            apiAccessible = false;
            return generateFallbackInterviewQuestions(cvContent);
        }
    }

    /**
     * Méthode pour poser une question à l'API Gemini.
     *
     * @param question La question posée.
     * @return La réponse de l'API Gemini.
     * @throws IOException En cas d'erreur réseau.
     */
    public String askQuestion(String question) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Construire l'URL complète avec la clé API
            String fullApiUrl = API_URL + "?key=" + apiKey;

            // Configurer la requête HTTP POST
            HttpPost post = new HttpPost(fullApiUrl);
            post.setHeader("Content-Type", "application/json");

            // Construire le JSON pour la requête
            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", question + " Veuillez répondre uniquement en français.");

            JsonArray partsArray = new JsonArray();
            partsArray.add(textPart);

            JsonObject contentsObject = new JsonObject();
            contentsObject.add("parts", partsArray);

            JsonArray contentsArray = new JsonArray();
            contentsArray.add(contentsObject);

            JsonObject requestBody = new JsonObject();
            requestBody.add("contents", contentsArray);

            // Ajouter le JSON dans le corps de la requête
            StringEntity entity = new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON);
            post.setEntity(entity);

            // Envoyer la requête et récupérer la réponse
            try (CloseableHttpResponse response = client.execute(post)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                logger.debug("API Response: {}", jsonResponse);

                // Extraire la réponse textuelle de l'API
                JsonObject responseObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                if (responseObject.has("candidates")) {
                    JsonArray candidates = responseObject.getAsJsonArray("candidates");

                    if (candidates != null && candidates.size() > 0) {
                        JsonObject firstCandidate = candidates.get(0).getAsJsonObject();

                        // La structure peut varier selon le modèle d'API
                        if (firstCandidate.has("content")) {
                            JsonObject content = firstCandidate.getAsJsonObject("content");
                            if (content.has("parts")) {
                                JsonArray parts = content.getAsJsonArray("parts");
                                if (parts != null && parts.size() > 0) {
                                    JsonObject firstPart = parts.get(0).getAsJsonObject();
                                    if (firstPart.has("text")) {
                                        return firstPart.get("text").getAsString();
                                    }
                                }
                            }
                        }
                    }
                }
                logger.warn("Format de réponse non reconnu ou erreur API: {}", jsonResponse);
            }
            return "";
        } catch (Exception e) {
            logger.error("Erreur lors de la communication avec l'API Gemini", e);
            return "";
        }
    }
} 