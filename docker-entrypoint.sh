#!/bin/bash
set -e

# Créer le répertoire d'upload s'il n'existe pas (au cas où le volume est vide)
mkdir -p /var/lib/facelink/uploads/profiles

# Définir les permissions pour permettre l'écriture
chmod -R 777 /var/lib/facelink/uploads

# Afficher les informations de debug
echo "Upload directory created and permissions set:"
ls -la /var/lib/facelink/uploads/

# Démarrer Tomcat
exec catalina.sh run
