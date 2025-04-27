package com.rh.cvfilter;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.util.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CVAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(CVAnalyzer.class);
    private StanfordCoreNLP pipeline;

    public CVAnalyzer() {
        Properties props = new Properties();
        // Retirer 'ner' des annotateurs pour éviter le problème avec JAXB
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        
        // Configurer le système pour JAXB avec Spring Boot 3
        System.setProperty("javax.xml.bind.context.factory", "org.glassfish.jaxb.runtime.v2.ContextFactory");
        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.glassfish.jaxb.runtime.v2.JAXBContextFactory");
        
        try {
            pipeline = new StanfordCoreNLP(props);
            logger.info("Pipeline NLP initialisée avec succès");
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation de la pipeline NLP", e);
            // Fallback en cas d'échec
            pipeline = null;
        }
    }

    public List<String> extractSkills(String cvContent) {
        List<String> skills = new ArrayList<>();
        
        if (pipeline == null) {
            // Méthode de secours en cas de problème avec Stanford NLP
            return extractSkillsFallback(cvContent);
        }
        
        try {
            CoreDocument document = new CoreDocument(cvContent);
            pipeline.annotate(document);
            for (CoreSentence sentence : document.sentences()) {
                for (CoreLabel token : sentence.tokens()) {
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    if (pos.startsWith("NN") && token.lemma().length() > 3) {
                        skills.add(token.lemma());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction des compétences", e);
            return extractSkillsFallback(cvContent);
        }
        return skills;
    }

    private List<String> extractSkillsFallback(String cvContent) {
        logger.info("Utilisation de la méthode de secours pour l'extraction des compétences");
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

    public List<String> extractExperience(String cvContent) {
        List<String> experiences = new ArrayList<>();
        
        if (pipeline == null) {
            // Méthode simple en cas de problème avec Stanford NLP
            return extractExperienceFallback(cvContent);
        }
        
        try {
            CoreDocument document = new CoreDocument(cvContent);
            pipeline.annotate(document);
            for (CoreSentence sentence : document.sentences()) {
                if (sentence.text().toLowerCase().contains("expérience") || sentence.text().toLowerCase().contains("travail")) {
                    experiences.add(sentence.text());
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction des expériences", e);
            return extractExperienceFallback(cvContent);
        }
        return experiences;
    }
    
    private List<String> extractExperienceFallback(String cvContent) {
        logger.info("Utilisation de la méthode de secours pour l'extraction des expériences");
        List<String> experiences = new ArrayList<>();
        String[] lines = cvContent.split("\n");
        for (String line : lines) {
            if (line.toLowerCase().contains("expérience") || line.toLowerCase().contains("travail")) {
                experiences.add(line);
            }
        }
        return experiences;
    }

    public List<String> extractEducation(String cvContent) {
        List<String> educations = new ArrayList<>();
        
        if (pipeline == null) {
            // Méthode simple en cas de problème avec Stanford NLP
            return extractEducationFallback(cvContent);
        }
        
        try {
            CoreDocument document = new CoreDocument(cvContent);
            pipeline.annotate(document);
            for (CoreSentence sentence : document.sentences()) {
                if (sentence.text().toLowerCase().contains("éducation") || 
                    sentence.text().toLowerCase().contains("diplôme") || 
                    sentence.text().toLowerCase().contains("formation")) {
                    educations.add(sentence.text());
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction des formations", e);
            return extractEducationFallback(cvContent);
        }
        return educations;
    }
    
    private List<String> extractEducationFallback(String cvContent) {
        logger.info("Utilisation de la méthode de secours pour l'extraction des formations");
        List<String> educations = new ArrayList<>();
        String[] lines = cvContent.split("\n");
        for (String line : lines) {
            if (line.toLowerCase().contains("éducation") || 
                line.toLowerCase().contains("diplôme") || 
                line.toLowerCase().contains("formation")) {
                educations.add(line);
            }
        }
        return educations;
    }

    public double calculateRelevanceScore(String cvContent, String jobOffer) {
        List<String> cvSkills = extractSkills(cvContent);
        List<String> jobSkills = extractSkills(jobOffer);
        
        if (jobSkills.isEmpty()) {
            return 0.0;
        }
        
        int commonSkills = 0;
        for (String skill : cvSkills) {
            if (jobSkills.contains(skill)) {
                commonSkills++;
            }
        }
        return (double) commonSkills / jobSkills.size();
    }
} 