# Étape 1: Utiliser une image de base contenant Java 11
FROM openjdk:11-jre-slim

# Étape 2: Définir le répertoire de travail dans le conteneur
WORKDIR /app

# Étape 3: Copier le fichier JAR généré (assurez-vous que le fichier JAR est dans le répertoire target)
COPY target/message-router-0.0.1-SNAPSHOT.jar app.jar

# Étape 4: Exposer le port 8080 pour accéder à l'application (par défaut, Spring Boot utilise le port 8080)
EXPOSE 8080

# Étape 5: Spécifier la commande de démarrage de l'application Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
