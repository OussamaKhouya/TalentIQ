package com.rh.cvfilter.api;

import com.rh.cvfilter.CVAnalyzer;
import com.rh.cvfilter.CVImporter;
import com.rh.cvfilter.CVSelector;
import com.rh.cvfilter.llm.CVAnalysisResult;
import com.rh.cvfilter.llm.JobMatchResult;
import com.rh.cvfilter.llm.LLMService;
import com.rh.cvfilter.model.CVInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contrôleur REST pour la gestion des CVs
 */
@RestController
public class CVApi {
    private static final Logger logger = LoggerFactory.getLogger(CVApi.class);
    private CVImporter importer = new CVImporter();
    private CVAnalyzer analyzer = new CVAnalyzer();
    private CVSelector selector = new CVSelector();
    
    // Définir le chemin du dossier pour stocker les fichiers CVs
    private static final String UPLOAD_DIR = "uploaded_cvs";
    
    @Autowired
    private LLMService llmService;
    
    @Value("${llm.provider:gemini}")
    private String llmProvider;

    /**
     * Endpoint principal de l'API
     */
    @GetMapping("/apiV2")
    public Map<String, String> apiInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "TalentIQ CV Filter API");
        info.put("version", "1.0");
        info.put("status", "active");
        return info;
    }
    
    /**
     * Endpoint pour vérifier le statut du LLM
     * @return Map avec les informations sur le LLM actif
     */
    @GetMapping("/apiV2/llm-status")
    public Map<String, Object> getLLMStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("provider", llmProvider);
        status.put("enabled", llmService.isApiKeyConfigured());
        status.put("accessible", llmService.isApiAccessible());
        status.put("fallbackMode", llmService.isApiKeyConfigured() && !llmService.isApiAccessible());
        return status;
    }

    /**
     * Endpoint pour traiter les CVs et les analyser par rapport à une offre d'emploi
     * @param cvFiles Fichiers CV à analyser
     * @param jobOffer Description de l'offre d'emploi
     * @return Liste des CVs analysés et triés par pertinence
     */
    @PostMapping("/apiV2/analyze")
    public ResponseEntity<Map<String, Object>> analyzeCVs(@RequestParam("cvFiles") MultipartFile[] cvFiles, @RequestParam("jobOffer") String jobOffer) {
        try {
            logger.info("Nombre de CVs uploadés: {}", cvFiles.length);
            
            if (cvFiles.length == 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Aucun CV n'a été fourni");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Créer le répertoire de stockage s'il n'existe pas
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Dossier de stockage créé: {}", uploadPath.toAbsolutePath());
            }
            
            // Structure pour stocker les fichiers et leurs contenus
            Map<String, String> fileToContent = new HashMap<>();
            Map<String, String> contentToDownloadId = new HashMap<>();
            
            // Traiter et sauvegarder chaque fichier CV
            for (MultipartFile cvFile : cvFiles) {
                String originalFilename = cvFile.getOriginalFilename();
                if (originalFilename == null) continue;
                
                // Générer un nom de fichier unique
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
                
                // Chemin complet du fichier
                Path filePath = uploadPath.resolve(uniqueFilename);
                
                // Sauvegarder le fichier
                Files.copy(cvFile.getInputStream(), filePath);
                logger.info("Fichier sauvegardé: {} avec ID: {}", originalFilename, uniqueFilename);
                
                // Extraire le contenu du fichier
                String content = extractContentFromFile(filePath.toFile());
                
                // Associer le contenu avec le fichier et le downloadId
                fileToContent.put(originalFilename, content);
                contentToDownloadId.put(content, uniqueFilename);
                
                logger.info("Mapped content to downloadId: {}", uniqueFilename);
            }
            
            // Récupérer les contenus de tous les CVs
            List<String> cvContents = new ArrayList<>(fileToContent.values());
            
            logger.info("Content to DownloadId mappings:");
            for (Map.Entry<String, String> entry : contentToDownloadId.entrySet()) {
                // Log only a part of the content to avoid huge logs
                String contentPreview = entry.getKey().length() > 50 
                    ? entry.getKey().substring(0, 50) + "..." 
                    : entry.getKey();
                logger.info("Content preview: [{}], downloadId: {}", contentPreview, entry.getValue());
            }
            
            // Sélectionner les meilleurs CVs
            List<String> topCVs = selector.selectTopCVs(
                cvContents, 
                jobOffer, 
                analyzer
            );
            
            logger.info("Nombre de CVs extraits: {}, Nombre de CVs sélectionnés: {}", 
                cvContents.size(), topCVs.size());
            
            // Créer les infos pour chaque CV sélectionné
            List<CVInfo> cvInfos = new ArrayList<>();
            for (String cvContent : topCVs) {
                String name = extractName(cvContent);
                String email = extractEmail(cvContent);
                String phone = extractPhone(cvContent);
                
                // Récupérer le downloadId correspondant au contenu
                String downloadId = contentToDownloadId.get(cvContent);
                logger.info("CV: {}, Content hash: {}, downloadId: {}", 
                    name, 
                    cvContent.hashCode(), 
                    downloadId != null ? downloadId : "NULL");
                
                // Si downloadId est null, essayer de trouver une correspondance partielle
                if (downloadId == null) {
                    logger.warn("downloadId est null pour {}. Tentative de correspondance partielle...", name);
                    
                    // Chercher une correspondance partielle dans le contenu
                    for (Map.Entry<String, String> entry : contentToDownloadId.entrySet()) {
                        String storedContent = entry.getKey();
                        String storedDownloadId = entry.getValue();
                        
                        // Vérifier si les premiers 200 caractères correspondent
                        int compareLength = Math.min(200, Math.min(cvContent.length(), storedContent.length()));
                        if (compareLength > 0) {
                            String cvContentStart = cvContent.substring(0, compareLength);
                            String storedContentStart = storedContent.substring(0, compareLength);
                            
                            if (cvContentStart.equals(storedContentStart)) {
                                downloadId = storedDownloadId;
                                logger.info("Correspondance partielle trouvée! downloadId: {}", downloadId);
                                break;
                            }
                        }
                    }
                    
                    // Si toujours pas trouvé, utiliser la première entrée disponible
                    if (downloadId == null && !contentToDownloadId.isEmpty()) {
                        downloadId = contentToDownloadId.values().iterator().next();
                        logger.warn("Utilisation du premier downloadId disponible: {}", downloadId);
                    }
                }
                
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
            
            Map<String, Object> response = new HashMap<>();
            response.put("candidates", cvInfos);
            response.put("llmInfo", Map.of(
                "enabled", llmService.isApiKeyConfigured(),
                "accessible", llmService.isApiAccessible(),
                "useFallback", llmService.isApiKeyConfigured() && !llmService.isApiAccessible(),
                "provider", llmProvider
            ));
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Erreur lors du traitement des CVs", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors du traitement des CVs: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Extrait le contenu textuel d'un fichier
     */
    private String extractContentFromFile(File file) {
        try {
            // Utiliser l'importateur de CV pour extraire le contenu
            String[] filename = new String[]{ file.getName() };
            List<String> contents = importer.importCVs(new File[]{ file }, filename);
            
            if (!contents.isEmpty()) {
                return contents.get(0);
            }
            return "";
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction du contenu du fichier: {}", file.getName(), e);
            return "";
        }
    }
    
    /**
     * Endpoint pour télécharger un CV analysé
     */
    @GetMapping("/apiV2/download/{fileId}")
    public ResponseEntity<Resource> downloadCV(@PathVariable String fileId) {
        try {
            // Construire le chemin complet du fichier
            Path filePath = Paths.get(UPLOAD_DIR, fileId);
            File file = filePath.toFile();
            
            if (file.exists()) {
                Resource resource = new FileSystemResource(file);
                
                // Déterminer le type MIME
                String contentType = "application/octet-stream";
                if (fileId.toLowerCase().endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (fileId.toLowerCase().endsWith(".txt")) {
                    contentType = "text/plain";
                }
                
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileId + "\"")
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                logger.error("Fichier non trouvé: {}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors du téléchargement du fichier", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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