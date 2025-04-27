# TalentIQ - API Documentation

TalentIQ est une application de filtrage et d'analyse de CV basée sur l'intelligence artificielle. Cette documentation détaille les endpoints REST disponibles dans l'API.

## Aperçu des endpoints

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/apiV2` | Informations générales sur l'API |
| GET | `/apiV2/llm-status` | Vérifier le statut du modèle de langage utilisé |
| POST | `/apiV2/analyze` | Analyser des CVs par rapport à une offre d'emploi |
| GET | `/apiV2/download/{fileId}` | Télécharger un CV analysé |

## Détails des endpoints

### Informations sur l'API

```
GET /apiV2
```

Retourne des informations générales sur l'API.

**Réponse**:
```json
{
  "name": "TalentIQ CV Filter API",
  "version": "1.0",
  "status": "active"
}
```

### Statut du modèle de langage

```
GET /apiV2/llm-status
```

Vérifie et retourne le statut du modèle de langage (LLM) utilisé.

**Réponse**:
```json
{
  "provider": "gemini",
  "enabled": true,
  "accessible": true,
  "fallbackMode": false
}
```

### Analyser des CVs

```
POST /apiV2/analyze
```

Analyse des CVs par rapport à une offre d'emploi spécifique.

**Paramètres**:
- `cvFiles` (multipart/form-data): Fichiers CV à analyser (PDF, TXT)
- `jobOffer` (string): Description textuelle de l'offre d'emploi

**Réponse**:
```json
{
  "candidates": [
    {
      "name": "John Doe",
      "email": "john.doe@example.com",
      "phone": "+33612345678",
      "downloadId": "550e8400-e29b-41d4-a716-446655440000.pdf",
      "skills": ["Java", "Spring Boot", "REST API"],
      "experience": "5 ans d'expérience en développement web",
      "education": "Master en informatique",
      "languages": ["Français", "Anglais"],
      "score": 85,
      "matchExplanation": "Forte correspondance avec l'offre d'emploi en raison...",
      "interviewQuestions": [
        "Parlez-moi de votre expérience avec Spring Boot",
        "Comment gérez-vous les problèmes de performance dans les API REST?"
      ]
    }
  ],
  "llmInfo": {
    "enabled": true,
    "accessible": true,
    "useFallback": false,
    "provider": "gemini"
  }
}
```

### Télécharger un CV

```
GET /apiV2/download/{fileId}
```

Télécharge un CV identifié par son ID.

**Paramètres**:
- `fileId` (path): Identifiant unique du fichier à télécharger (UUID avec extension)

**Réponse**:
- Le fichier CV au format d'origine

## Codes d'erreur

| Code | Description |
|------|-------------|
| 200 | Succès |
| 400 | Requête invalide (paramètres manquants ou incorrects) |
| 404 | Ressource non trouvée |
| 500 | Erreur interne du serveur |

## Notes d'implémentation

- Les fichiers CV uploadés sont stockés dans le répertoire `uploaded_cvs` à la racine du projet
- Les identifiants de téléchargement (downloadId) sont des UUID uniques avec l'extension du fichier original
- Le système extrait automatiquement le nom, l'email, et le numéro de téléphone des CV
- Si un modèle de langage (LLM) est configuré, le système enrichit l'analyse avec des compétences, expériences, et questions d'entretien 