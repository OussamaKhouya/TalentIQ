@echo off
echo Test de connectivite avec Ollama
echo.

REM Verifier si Ollama est en cours d'execution
tasklist /FI "IMAGENAME eq ollama.exe" 2>NUL | find /I /N "ollama.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo Ollama est en cours d'execution.
) else (
    echo ERREUR: Ollama n'est pas en cours d'execution!
    echo Veuillez demarrer Ollama avant d'executer ce test.
    pause
    exit /b 1
)

echo.
echo Verification de l'API Ollama...
echo.

REM Construire la requete JSON pour Ollama
echo {"model": "llama3", "prompt": "Bonjour, es-tu disponible?", "stream": false} > ollama_test_request.json

REM Envoyer la requete a l'API Ollama
curl -s -X POST http://localhost:11434/api/generate -d @ollama_test_request.json > ollama_test_response.json

REM Supprimer le fichier de requete
del ollama_test_request.json

REM Verifier si la requete a reussi
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: La requete a l'API Ollama a echoue.
    echo Verifiez que Ollama fonctionne correctement sur le port 11434.
    del ollama_test_response.json 2>NUL
    pause
    exit /b 1
)

echo L'API Ollama est accessible.
echo.

REM Verifier les modeles disponibles
echo Modeles disponibles dans Ollama:
curl -s http://localhost:11434/api/tags

echo.
echo Test de l'utilisation du modele "llama3"
echo (Si ce modele n'est pas disponible, utilisez "ollama pull llama3")
echo.

REM Afficher la reponse du modele
echo Reponse du modele:
echo.
type ollama_test_response.json | findstr /C:"response"

REM Supprimer le fichier de reponse
del ollama_test_response.json

echo.
echo Test termine.
echo.
echo Si vous voyez une reponse ci-dessus, Ollama fonctionne correctement.
echo Si vous ne voyez pas de reponse, verifiez que le modele "llama3" est installe.

pause 