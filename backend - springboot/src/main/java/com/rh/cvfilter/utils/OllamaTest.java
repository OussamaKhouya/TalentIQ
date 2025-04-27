package com.rh.cvfilter.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

/**
 * Utilitaire pour tester la connectivité avec Ollama
 * Peut être exécuté comme une application autonome
 * 
 * Usage: java -classpath ... com.rh.cvfilter.utils.OllamaTest "Votre prompt ici"
 */
public class OllamaTest {
    
    private static final String DEFAULT_API_URL = "http://localhost:11434/api/generate";
    private static final String DEFAULT_MODEL = "llama3.1";
    
    public static void main(String[] args) {
        String apiUrl = System.getProperty("ollama.api.url", DEFAULT_API_URL);
        String model = System.getProperty("ollama.model", DEFAULT_MODEL);
        
        String prompt;
        if (args.length > 0) {
            prompt = args[0];
        } else {
            prompt = "Quelle est la capitale de la France ? Répondez en une phrase simple.";
        }
        
        System.out.println("Test de connectivité avec Ollama :");
        System.out.println("URL de l'API : " + apiUrl);
        System.out.println("Modèle : " + model);
        System.out.println("Prompt : " + prompt);
        System.out.println("\nEnvoi de la requête...");
        
        try {
            String response = sendPrompt(apiUrl, model, prompt);
            System.out.println("\nRéponse reçue :\n");
            System.out.println(response);
            System.out.println("\nTest réussi !");
        } catch (Exception e) {
            System.err.println("\nErreur lors de la communication avec Ollama :");
            e.printStackTrace();
            System.err.println("\nVérifiez que :");
            System.err.println("1. Ollama est installé et en cours d'exécution");
            System.err.println("2. Le modèle '" + model + "' est disponible (utilisez 'ollama pull " + model + "')");
            System.err.println("3. L'URL de l'API est correcte : " + apiUrl);
        }
    }
    
    private static String sendPrompt(String apiUrl, String model, String prompt) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(apiUrl);
            post.setHeader("Content-Type", "application/json");
            
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.addProperty("prompt", prompt);
            requestBody.addProperty("stream", false);
            
            StringEntity entity = new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON);
            post.setEntity(entity);
            
            try (CloseableHttpResponse response = client.execute(post)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                
                JsonObject responseObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                if (responseObject.has("response")) {
                    return responseObject.get("response").getAsString();
                } else {
                    throw new RuntimeException("Réponse Ollama non reconnue : " + jsonResponse);
                }
            }
        }
    }
} 