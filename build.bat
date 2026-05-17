@echo off
title ItsVanillaEssential - Build
color 0A

echo.
echo  =============================================
echo   ItsVanillaEssential - Build automatique
echo  =============================================
echo.

:: ── Vérifier Java ──────────────────────────────────────────────
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] Java n'est pas installe ou introuvable dans le PATH.
    echo Telechargez Java 21 sur : https://adoptium.net
    pause
    exit /b 1
)

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VER=%%g
)
echo [OK] Java detecte : %JAVA_VER%

:: ── Vérifier Maven ─────────────────────────────────────────────
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] Maven n'est pas installe ou introuvable dans le PATH.
    echo Telechargez Maven sur : https://maven.apache.org/download.cgi
    echo Puis ajoutez Maven/bin au PATH systeme.
    pause
    exit /b 1
)
echo [OK] Maven detecte.

:: ── Build ───────────────────────────────────────────────────────
echo.
echo [BUILD] Compilation en cours...
echo.

mvn clean package -f "%~dp0pom.xml"

if %errorlevel% neq 0 (
    echo.
    echo [ERREUR] La compilation a echoue. Consultez les logs ci-dessus.
    pause
    exit /b 1
)

:: ── Résultat ────────────────────────────────────────────────────
echo.
echo  =============================================
echo   BUILD REUSSI !
echo  =============================================
echo.
echo  Votre .jar se trouve dans :
echo  %~dp0target\ItsVanillaEssential-1.0.0.jar
echo.
echo  Copiez ce fichier dans le dossier plugins\
echo  de votre serveur Spigot/Paper 1.21.1
echo.
pause
