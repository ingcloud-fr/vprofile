# Stage 1: Build avec Maven
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copier pom.xml et télécharger dépendances (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Build l'application
RUN mvn clean package -DskipTests

# Stage 2: Runtime avec Tomcat
FROM tomcat:10.1-jdk17-temurin-jammy

# Installer unzip pour décompresser le WAR
RUN apt-get update && apt-get install -y unzip && rm -rf /var/lib/apt/lists/*

# Supprimer les apps par défaut de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copier le WAR depuis le stage de build
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Décompresser le WAR manuellement
RUN cd /usr/local/tomcat/webapps && \
    unzip -q ROOT.war -d ROOT && \
    rm ROOT.war

# Créer le répertoire pour les uploads persistants (volume Docker)
RUN mkdir -p /var/lib/vprofile/uploads/profiles

# Exposer le port
EXPOSE 8080

# Démarrer Tomcat
CMD ["catalina.sh", "run"]