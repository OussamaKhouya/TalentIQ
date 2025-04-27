package com.rh.cvfilter.controller;

import com.rh.cvfilter.CVAnalyzer;
import com.rh.cvfilter.CVImporter;
import com.rh.cvfilter.CVSelector;
import com.rh.cvfilter.llm.CVAnalysisResult;
import com.rh.cvfilter.llm.JobMatchResult;
import com.rh.cvfilter.llm.LLMService;
import com.rh.cvfilter.model.CVInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class CVController {
    private static final Logger logger = LoggerFactory.getLogger(CVController.class);
    private CVImporter importer = new CVImporter();
    private CVAnalyzer analyzer = new CVAnalyzer();
    private CVSelector selector = new CVSelector();
    private Map<String, File> temporaryFiles = new HashMap<>();
    
    @Autowired
    private LLMService llmService;
    
    @Value("${llm.provider:gemini}")
    private String llmProvider;

    @GetMapping("/")
    public String home() {
        return "home";
    }
    
    /**
     * Endpoint pour vérifier le statut du LLM
     * @return Map avec les informations sur le LLM actif
     */
    @GetMapping("/api/llm-status")
    @ResponseBody
    public Map<String, Object> getLLMStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("provider", llmProvider);
        status.put("enabled", llmService.isApiKeyConfigured());
        status.put("accessible", llmService.isApiAccessible());
        status.put("fallbackMode", llmService.isApiKeyConfigured() && !llmService.isApiAccessible());
        return status;
    }

    @PostMapping("/upload")
    public String uploadCVs(@RequestParam("cvFiles") MultipartFile[] cvFiles, @RequestParam("jobOffer") String jobOffer, Model model) throws IOException {
        logger.info("Nombre de CVs uploadés: {}", cvFiles.length);
        File[] files = new File[cvFiles.length];
        String[] originalFilenames = new String[cvFiles.length];
        Map<String, String> contentToFileMap = new HashMap<>();
        for (int i = 0; i < cvFiles.length; i++) {
            logger.info("Traitement du fichier: {}", cvFiles[i].getOriginalFilename());
            File file = File.createTempFile("cv_", ".tmp");
            cvFiles[i].transferTo(file);
            files[i] = file;
            originalFilenames[i] = cvFiles[i].getOriginalFilename();
            String uniqueId = UUID.randomUUID().toString();
            temporaryFiles.put(uniqueId, file);
            contentToFileMap.put(originalFilenames[i], uniqueId);
            logger.info("Fichier temporaire créé: {}", file.getAbsolutePath());
        }
        List<String> cvContents = importer.importCVs(files, originalFilenames);
        logger.info("Nombre de contenus de CV extraits: {}", cvContents.size());
        List<String> topCVs = selector.selectTopCVs(cvContents, jobOffer, analyzer);
        logger.info("Nombre de CVs sélectionnés: {}", topCVs.size());
        
        List<CVInfo> cvInfos = new ArrayList<>();
        for (String cvContent : topCVs) {
            String name = extractName(cvContent);
            String email = extractEmail(cvContent);
            String phone = extractPhone(cvContent);
            String downloadId = contentToFileMap.entrySet().stream()
                .filter(entry -> cvContents.contains(cvContent) && cvContents.indexOf(cvContent) < cvFiles.length && cvFiles[cvContents.indexOf(cvContent)].getOriginalFilename().equals(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst().orElse("");
            
            CVInfo cvInfo = new CVInfo(name, email, phone, downloadId);
            
            // Enrichir avec LLM si une clé API est configurée
            if (llmService.isApiKeyConfigured()) {
                try {
                    // Analyse du CV
                    CVAnalysisResult analysisResult = llmService.analyzeCVWithLLM(cvContent);
                    cvInfo.setSkills(analysisResult.getSkills());
                    cvInfo.setExperience(analysisResult.getExperience());
                    cvInfo.setEducation(analysisResult.getEducation());
                    cvInfo.setLanguages(analysisResult.getLanguages());
                    
                    // Évaluation de la correspondance avec l'offre
                    JobMatchResult matchResult = llmService.evaluateCVForJobOffer(cvContent, jobOffer);
                    cvInfo.setScore(matchResult.getScore());
                    cvInfo.setMatchExplanation(matchResult.getExplanation());
                    
                    // Génération de questions d'entretien
                    List<String> questions = llmService.generateInterviewQuestions(cvContent, jobOffer);
                    cvInfo.setInterviewQuestions(questions);
                    
                    logger.info("Analyse LLM réussie pour le CV: {}", name);
                } catch (Exception e) {
                    logger.error("Erreur lors de l'analyse LLM du CV", e);
                }
            }
            
            cvInfos.add(cvInfo);
        }
        
        // Trier les CV par score (si disponible)
        cvInfos.sort((cv1, cv2) -> Integer.compare(cv2.getScore(), cv1.getScore()));
        
        model.addAttribute("topCVs", cvInfos);
        model.addAttribute("llmEnabled", llmService.isApiKeyConfigured());
        model.addAttribute("apiAccessible", llmService.isApiAccessible());
        model.addAttribute("useFallback", llmService.isApiKeyConfigured() && !llmService.isApiAccessible());
        model.addAttribute("llmProvider", llmProvider);
        return "result";
    }
    
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadCV(@PathVariable String fileId) {
        File file = temporaryFiles.get(fileId);
        if (file != null && file.exists()) {
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    private String extractName(String content) {
        // Recherche basée sur des mots-clés comme 'Nom', 'Name', ou des titres
        Pattern namePattern = Pattern.compile("(?i)(Nom\\s*:|Name\\s*:|Prénom\\s*:|First\\s*Name\\s*:|Last\\s*Name\\s*:)?\\s*([A-Za-zÀ-ÿ]+\\s+[A-Za-zÀ-ÿ]+(?:\\s+[A-Za-zÀ-ÿ]+)?)");
        Matcher matcher = namePattern.matcher(content);
        if (matcher.find()) {
            String name = matcher.group(2).trim();
            if (!name.isEmpty()) {
                return name;
            }
        }
        // Recherche de secours : chercher des lignes qui ressemblent à un nom (2 ou 3 mots avec majuscules)
        String[] lines = content.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.matches("[A-Za-zÀ-ÿ]+\\s+[A-Za-zÀ-ÿ]+(?:\\s+[A-Za-zÀ-ÿ]+)?") && line.split("\\s+").length <= 3) {
                // Vérifier si au moins un mot commence par une majuscule
                String[] words = line.split("\\s+");
                for (String word : words) {
                    if (word.length() > 1 && Character.isUpperCase(word.charAt(0))) {
                        return line;
                    }
                }
            }
        }
        return "Nom non trouvé";
    }
    
    private String extractEmail(String content) {
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher matcher = emailPattern.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }
        return "Email non trouvé";
    }
    
    private String extractPhone(String content) {
        // Recherche de numéros de téléphone au format français et international
        Pattern phonePattern = Pattern.compile("(?:\\+33|0)\\s*[1-9](?:[\\s.-]*\\d{2}){4}");
        Matcher matcher = phonePattern.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }
        return "Téléphone non trouvé";
    }
} 