# BankApp Microservices

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.17-green)
![Maven](https://img.shields.io/badge/Maven-3.8+-blue)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

## 🏗️ Overview

BankApp Microservices est une application back-end modulaire pour la gestion bancaire, basée sur l’architecture microservices. Chaque service est indépendant et communique via REST ou IBM MQ.

## 📦 Services inclus

* **Auth Service** – Gestion de l’authentification et des tokens JWT via Keycloak
* **HR Service** – Gestion des employés et départements
* **Mission Service** – Gestion des missions bancaires
* **Message Router** – Routeur de messages IBM MQ
* **Gateway Service** – API Gateway avec sécurisation OAuth2/JWT
* **Eureka Server** – Découverte et enregistrement des services

## 🛠️ Tech Stack

* Java 17
* Spring Boot 2.7
* Spring Cloud Netflix Eureka
* Spring Security & OAuth2 Resource Server
* Keycloak
* IBM MQ
* PostgreSQL & H2
* Docker & Docker Compose
* Swagger/OpenAPI
* Maven

## ⚙️ Prérequis

* Java 17
* Maven 3.8+
* Docker & Docker Compose
* Keycloak (local ou distant)
* IBM MQ (local ou distant)

## 🚀 Lancer le projet localement

1. Cloner le projet :

   ```bash
   git clone https://github.com/youssefJmaiel/bankapp-microservice.git
   cd bankapp-microservice
   ```
2. Configurer Keycloak :

   * Realm : `spring-app`
   * Client : `spring-boot-client`
3. Lancer tous les services via Docker Compose :

   ```bash
   docker-compose up --build
   ```
4. Vérifier l’état des services sur Eureka Server :
   [http://localhost:8761/](http://localhost:8761/)

## 🌐 Endpoints

* **Auth Service** : `/auth/**`
* **HR Service** : `/api/employees` ([http://localhost:8083](http://localhost:8083))
* **Mission Service** : `/mission/**` ([http://localhost:8084](http://localhost:8084))
* **Message Router** : `/api/messages` ([http://localhost:8085](http://localhost:8085))
* **Gateway** : centralise toutes les requêtes et applique la sécurité

## 🔒 Sécurité

* OAuth2 / JWT via Keycloak
* Routes protégées sauf `/swagger-ui/**` et `/v3/api-docs/**`
* Exemple d’en-tête :

  ```
  Authorization: Bearer <JWT_TOKEN>
  ```

## 📑 Documentation API

* Swagger disponible sur `/swagger-ui/index.html` pour chaque service
* Exemple HR Service : [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)

## 🐳 Docker

* Chaque service possède son propre Dockerfile
* `docker-compose.yml` permet de lancer tous les services ensemble
* Bases de données : H2 pour dev, PostgreSQL pour prod

## 🤝 Contribution

1. Fork le projet
2. Créer une branche `feature/nom-feature`
3. Commit et push sur ta branche
4. Ouvrir un Pull Request

## 📜 License

MIT License
