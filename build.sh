#!/bin/bash
echo ""
echo "============================================="
echo "  ItsVanillaEssential - Build automatique"
echo "============================================="
echo ""

# ── Vérifier Java ──────────────────────────────────────────────
if ! command -v java &> /dev/null; then
    echo "[ERREUR] Java n'est pas installé."
    echo "Téléchargez Java 21 sur : https://adoptium.net"
    exit 1
fi
echo "[OK] Java : $(java -version 2>&1 | head -1)"

# ── Vérifier Maven ─────────────────────────────────────────────
if ! command -v mvn &> /dev/null; then
    echo ""
    echo "[ERREUR] Maven n'est pas installé."
    echo ""
    echo "Sur Mac (Homebrew)  : brew install maven"
    echo "Sur Ubuntu/Debian   : sudo apt install maven"
    echo "Sur Fedora/RHEL     : sudo dnf install maven"
    exit 1
fi
echo "[OK] Maven : $(mvn -version 2>&1 | head -1)"

# ── Build ───────────────────────────────────────────────────────
echo ""
echo "[BUILD] Compilation en cours..."
echo ""

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
mvn clean package -f "$SCRIPT_DIR/pom.xml"

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERREUR] La compilation a échoué. Consultez les logs ci-dessus."
    exit 1
fi

echo ""
echo "============================================="
echo "  BUILD RÉUSSI !"
echo "============================================="
echo ""
echo "  Votre .jar se trouve dans :"
echo "  $SCRIPT_DIR/target/ItsVanillaEssential-1.0.0.jar"
echo ""
echo "  Copiez ce fichier dans le dossier plugins/"
echo "  de votre serveur Spigot/Paper 1.21.1"
echo ""
