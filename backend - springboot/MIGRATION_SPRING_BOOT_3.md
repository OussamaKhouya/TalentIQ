# Migration vers Spring Boot 3

Ce document décrit les modifications apportées pour migrer l'application TalentIQ (CV-Filter) de Spring Boot 2.7.5 vers Spring Boot 3.2.0.

## Changements principaux

1. **Mise à jour des dépendances dans le pom.xml**
   - Spring Boot parent mis à jour de 2.7.5 à 3.2.0
   - Mise à jour du plugin spring-boot-maven-plugin vers la version 3.2.0
   - Ajout de la dépendance jakarta.servlet-api 6.0.0
   - Migration de org.apache.httpcomponents:httpclient (4.5.14) vers org.apache.httpcomponents.client5:httpclient5 (5.2.1)
   - Ajout des dépendances JAXB pour être compatible avec Spring Boot 3:
     - jakarta.xml.bind-api
     - jaxb-runtime
     - jakarta.activation-api

2. **Modifications des imports dans le LLMService**
   - Remplacement des imports de org.apache.http.* par les versions correspondantes de org.apache.hc.client5.* et org.apache.hc.core5.*
   - Mise à jour de l'utilisation de StringEntity avec ContentType

3. **Configuration supplémentaire**
   - Ajout d'une classe de configuration WebConfig pour gérer les configurations spécifiques à Spring Boot 3
   - Ajout des propriétés système pour JAXB dans application.properties
   - Création d'un fichier metadata-complete.xml vide dans META-INF

4. **Modifications pour Stanford NLP**
   - Simplification de l'utilisation de Stanford NLP dans CVAnalyzer
   - Retrait de l'annotateur 'ner' qui causait des problèmes avec JAXB
   - Ajout de mécanismes de secours (fallback) pour garantir la fonctionnalité même si Stanford NLP échoue
   - Injection de propriétés système pour configurer JAXB correctement

## Changements à apporter à l'application lors du développement

1. **Vérifier le namespaces javax à jakarta**
   - Spring Boot 3 utilise Jakarta EE au lieu de Java EE
   - Tous les imports javax.* doivent être remplacés par jakarta.*

2. **Classes dépréciées dans Spring Boot 3**
   - Vérifier et mettre à jour toutes les classes/méthodes dépréciées

3. **Java 17 minimum**
   - Spring Boot 3 nécessite Java 17 au minimum (déjà configuré dans ce projet)

## Points d'attention

- Les API REST existantes devraient fonctionner sans modification majeure
- Les templates Thymeleaf devraient être compatibles
- Les propriétés de configuration Spring dans application.properties sont conservées
- La gestion des fichiers multipart doit être testée après la migration

## Résolution des problèmes courants

### Problème avec JAXB et Stanford NLP

Si vous rencontrez cette erreur:
```
javax.xml.bind.JAXBException: Implementation of JAXB-API has not been found on module path or classpath.
```

Solution mise en place:
1. Ajout des dépendances JAXB nécessaires dans pom.xml
2. Configuration de propriétés système dans application.properties
3. Mise à jour de CVAnalyzer pour gérer les erreurs liées à JAXB
4. Création d'un fichier metadata-complete.xml vide dans META-INF

## Tests recommandés

Après la migration, veuillez tester en priorité:
1. Le chargement et l'analyse des fichiers PDF
2. La communication avec l'API Gemini
3. La génération des résultats d'analyse
4. L'affichage correct des pages Thymeleaf

Si vous rencontrez des problèmes, référez-vous au guide de migration officiel de Spring Boot: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide 