package com.rh.cvfilter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CVImporter {
    private static final Logger logger = LoggerFactory.getLogger(CVImporter.class);

    public List<String> importCVs(File[] files) throws IOException {
        List<String> cvContents = new ArrayList<>();
        logger.info("Début de l'importation de {} fichiers", files.length);
        for (File file : files) {
            logger.info("Traitement du fichier: {}", file.getName());
            String content = extractContent(file);
            if (content != null) {
                logger.info("Contenu extrait pour {}: {} caractères", file.getName(), content.length());
                cvContents.add(content);
            } else {
                logger.warn("Aucun contenu extrait pour {}", file.getName());
            }
        }
        logger.info("Fin de l'importation, {} contenus extraits", cvContents.size());
        return cvContents;
    }

    public List<String> importCVs(File[] files, String[] originalFilenames) throws IOException {
        List<String> cvContents = new ArrayList<>();
        logger.info("Début de l'importation de {} fichiers", files.length);
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String originalFilename = originalFilenames[i];
            logger.info("Traitement du fichier: {}", originalFilename);
            String content = extractContent(file, originalFilename);
            if (content != null) {
                logger.info("Contenu extrait pour {}: {} caractères", originalFilename, content.length());
                cvContents.add(content);
            } else {
                logger.warn("Aucun contenu extrait pour {}", originalFilename);
            }
        }
        logger.info("Fin de l'importation, {} contenus extraits", cvContents.size());
        return cvContents;
    }

    private String extractContent(File file) throws IOException {
        String extension = getFileExtension(file);
        logger.info("Extension du fichier {}: {}", file.getName(), extension);
        if (extension.equalsIgnoreCase("pdf")) {
            return extractFromPDF(file);
        } else if (extension.equalsIgnoreCase("txt")) {
            return extractFromText(file);
        }
        logger.warn("Format non supporté pour {}", file.getName());
        return null;
    }

    private String extractContent(File file, String originalFilename) throws IOException {
        String extension = getFileExtension(originalFilename);
        logger.info("Extension du fichier {}: {}", originalFilename, extension);
        if (extension.equalsIgnoreCase("pdf")) {
            return extractFromPDF(file);
        } else if (extension.equalsIgnoreCase("txt")) {
            return extractFromText(file);
        }
        logger.warn("Format non supporté pour {}", originalFilename);
        return null;
    }

    private String extractFromPDF(File file) throws IOException {
        logger.info("Extraction PDF pour {}", file.getName());
        try (PDDocument document = PDDocument.load(file)) {
            if (!document.isEncrypted()) {
                PDFTextStripper textStripper = new PDFTextStripper();
                String content = textStripper.getText(document);
                logger.info("PDF extrait pour {}, contenu: {} caractères", file.getName(), content.length());
                return content;
            } else {
                logger.warn("PDF encrypté: {}", file.getName());
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction PDF pour {}: {}", file.getName(), e.getMessage());
        }
        return null;
    }

    private String extractFromText(File file) throws IOException {
        logger.info("Extraction texte pour {}", file.getName());
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            logger.info("Texte extrait pour {}, contenu: {} caractères", file.getName(), content.length());
            return content;
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction texte pour {}: {}", file.getName(), e.getMessage());
            return null;
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf + 1);
    }

    private String getFileExtension(String filename) {
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf + 1);
    }
} 