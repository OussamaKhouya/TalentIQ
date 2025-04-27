# Configuration Ollama pour TalentIQ

Ce document explique comment configurer et utiliser Ollama comme alternative à l'API Gemini pour le traitement des CV dans l'application TalentIQ.

## Prérequis

Pour utiliser Ollama, vous devez d'abord l'installer sur votre machine :

1. Téléchargez et installez Ollama depuis [le site officiel](https://ollama.com/)
2. Assurez-vous qu'Ollama est en cours d'exécution sur le port par défaut (11434)

## Configuration des modèles Ollama

Une fois Ollama installé, vous devez télécharger un modèle compatible. Nous recommandons Llama3 pour de bons résultats en français :

```bash
ollama pull llama3
```

Autres modèles recommandés :
- `mistral` - Bon équilibre entre performance et ressources requises
- `gemma:7b` - Bon modèle de Google qui fonctionne bien pour des tâches d'analyse

## Configuration de l'application

Pour utiliser Ollama au lieu de Gemini, modifiez le fichier `application.properties` :

```properties
# Changer le provider de LLM (options: gemini, ollama)
llm.provider=ollama

# Configuration Ollama
ollama.api.url=http://localhost:11434/api/generate
ollama.model=llama3
```

Vous pouvez spécifier le modèle que vous souhaitez utiliser en modifiant la propriété `ollama.model`.

## Utilisation

Après avoir configuré Ollama et redémarré l'application, toutes les fonctionnalités de traitement de CV utiliseront le modèle local Ollama plutôt que l'API Gemini.

## Dépannage

Si vous rencontrez des problèmes avec Ollama :

1. Vérifiez que le service Ollama est en cours d'exécution
```bash
# Sur Windows, vérifiez le processus
tasklist | findstr ollama

# Sur Linux/macOS
ps aux | grep ollama
```

2. Vérifiez que vous pouvez accéder à l'API Ollama
```bash
curl -X POST http://localhost:11434/api/generate -d '{"model": "llama3", "prompt": "Hello, how are you?", "stream": false}'
```

3. Vérifiez les journaux de l'application pour les erreurs

## Ressources système requises

L'utilisation d'un modèle local comme Llama3 peut nécessiter des ressources significatives :
- Au moins 8 Go de RAM
- Un processeur récent avec AVX2 (ou meilleur avec une carte graphique compatible)
- Au moins 10 Go d'espace disque pour le modèle

## Limitations

- Les modèles locaux peuvent être moins précis que l'API Gemini pour certaines tâches
- Le temps de traitement peut être plus long selon votre matériel
- La qualité des résultats peut varier selon le modèle choisi 