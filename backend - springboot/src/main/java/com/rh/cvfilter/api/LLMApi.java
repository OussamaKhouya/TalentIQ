package com.rh.cvfilter.api;

import com.rh.cvfilter.llm.GeminiLLMService;
import com.rh.cvfilter.llm.LLMService;
import com.rh.cvfilter.llm.OllamaLLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Contrôleur REST pour les informations et configurations liées au LLM
 */
@RestController
@RequestMapping("/apiV2/llm")
public class LLMApi {
    
    @Autowired
    private LLMService llmService;
    
    @Autowired
    private GeminiLLMService geminiService;
    
    @Autowired
    private OllamaLLMService ollamaService;
    
    @Autowired
    private Environment env;
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    @Value("${llm.provider:gemini}")
    private String llmProvider;
    
    @Value("${gemini.api.key:}")
    private String geminiApiKey;
    
    @Value("${ollama.api.url:http://localhost:11434/api/generate}")
    private String ollamaApiUrl;
    
    @Value("${ollama.model:llama3}")
    private String ollamaModel;
    
    /**
     * Endpoint pour récupérer le statut du LLM
     */
    @GetMapping("/status")
    public Map<String, Object> getLLMStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("provider", llmService.getActiveProvider());
        status.put("enabled", llmService.isApiKeyConfigured());
        status.put("accessible", llmService.isApiAccessible());
        status.put("fallbackMode", llmService.isApiKeyConfigured() && !llmService.isApiAccessible());
        
        // Configuration Gemini
        status.put("geminiApiKey", geminiApiKey.isEmpty() ? "Non configurée" : 
                  (geminiApiKey.length() > 8 ? 
                   geminiApiKey.substring(0, 4) + "..." + geminiApiKey.substring(geminiApiKey.length() - 4) : 
                   "Configurée"));
        
        // Configuration Ollama
        status.put("ollamaApiUrl", ollamaApiUrl);
        status.put("ollamaModel", ollamaModel);
        
        return status;
    }
    
    /**
     * API pour tester un modèle LLM spécifique
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> testLLM(@RequestParam String provider, @RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Prompt requis");
            return ResponseEntity.badRequest().body(response);
        }
        
        Map<String, String> result = new HashMap<>();
        try {
            String response;
            if ("gemini".equals(provider)) {
                response = testGemini(prompt);
            } else if ("ollama".equals(provider)) {
                response = testOllama(prompt);
            } else {
                result.put("error", "Provider non reconnu: " + provider);
                return ResponseEntity.badRequest().body(result);
            }
            
            result.put("response", response);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * Test direct de l'API Gemini
     */
    private String testGemini(String prompt) {
        try {
            return geminiService.askQuestion(prompt);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la communication avec Gemini: " + e.getMessage(), e);
        }
    }
    
    /**
     * Test direct de l'API Ollama
     */
    private String testOllama(String prompt) {
        try {
            return ollamaService.askQuestion(prompt);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la communication avec Ollama: " + e.getMessage(), e);
        }
    }
    
    /**
     * Changer de provider LLM dynamiquement
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> configureLLM(@RequestParam String provider) {
        Map<String, String> result = new HashMap<>();
        
        if (!"gemini".equals(provider) && !"ollama".equals(provider)) {
            result.put("error", "Provider non reconnu: " + provider);
            return ResponseEntity.badRequest().body(result);
        }
        
        try {
            // Mettre à jour le provider dans le service (changement en mémoire)
            llmService.setActiveProvider(provider);
            
            // Sauvegarder également le changement dans le fichier de configuration
            // pour qu'il soit persistant au redémarrage
            updateAppProperties(provider);
            
            result.put("success", "Provider changé avec succès en " + provider);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", "Erreur lors du changement de provider: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * Met à jour le fichier application.properties
     */
    private void updateAppProperties(String newProvider) throws IOException {
        // Chemin vers le fichier application.properties
        String[] paths = {
            "src/main/resources/application.properties", // Chemin pendant le développement
            "config/application.properties",             // Chemin possible en production
            "./application.properties"                   // Chemin à la racine
        };
        
        File propertiesFile = null;
        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                propertiesFile = file;
                break;
            }
        }
        
        if (propertiesFile == null) {
            throw new IOException("Fichier application.properties introuvable");
        }
        
        // Créer une copie de sauvegarde
        Path backupPath = Paths.get(propertiesFile.getParentFile().getPath(), "application.properties.bak");
        Files.copy(propertiesFile.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Charger les propriétés
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(propertiesFile)) {
            props.load(in);
        }
        
        // Mettre à jour la propriété
        props.setProperty("llm.provider", newProvider);
        
        // Enregistrer les modifications
        try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
            props.store(out, "Updated by LLMController");
        }
    }
} 