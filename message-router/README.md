# Message Router Backend

![Java](https://img.shields.io/badge/Java-11-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-2.x-green)
![Keycloak](https://img.shields.io/badge/Auth-Keycloak-orange)
![IBM MQ](https://img.shields.io/badge/Messaging-IBM%20MQ-red)
![Docker](https://img.shields.io/badge/Container-Docker-blue)
![Build](https://img.shields.io/badge/build-passing-brightgreen)

Backend service for a **Banking Message Routing System** built using **Spring Boot**.

The application integrates **IBM MQ messaging**, stores messages in a database, and exposes **secure REST APIs** to manage messages and partners.

The project demonstrates **enterprise backend architecture** including:

* Messaging middleware integration
* OAuth2 authentication with Keycloak
* Pagination and sorting
* Global exception handling
* Unit testing

---

# Features

* IBM MQ integration (send and receive messages)
* Message storage and management
* Partner management
* Secure REST APIs
* Pagination and sorting
* Role-based access control (Keycloak)
* Global exception handling
* DTO validation
* Unit testing
* Docker ready
* Secure configuration using `application.yml.example`

---

# Tech Stack

* Java 11
* Spring Boot 2.x
* Spring Data JPA
* Spring Security
* OAuth2 Resource Server
* Keycloak
* IBM MQ
* PostgreSQL / H2
* Maven
* Docker
* JUnit
* Mockito

---

# Architecture

```
                +-------------+
                |   Keycloak  |
                | Authentication |
                +------+------+
                       |
                       |
                +------v------+
                | Spring Boot |
                | Message Router |
                +------+------+
                       |
        +--------------+-------------+
        |                            |
  +-----v-----+               +------v------+
  |  IBM MQ   |               | PostgreSQL  |
  | Messaging |               | Database    |
  +-----------+               +-------------+
```

---

# Project Structure

```
message-router-backend
│
├── config
│   ├── GlobalExceptionHandler.java
│   ├── JacksonConfig.java
│   ├── MqConfig.java
│   ├── MqConfigProperties.java
│   └── SecurityConfig.java
│
├── controller
│   ├── MessageController.java
│   └── PartnerController.java
│
├── service
│   ├── MessageService.java
│   ├── PartnerService.java
│   ├── MqService.java
│   └── MqMessageListener.java
│
├── repository
│   ├── MessageRepository.java
│   └── PartnerRepository.java
│
├── entity
│   ├── Message.java
│   ├── Partner.java
│   ├── PartnerDirection.java
│   ├── PartnerType.java
│   └── ProcessedFlowType.java
│
├── dto
│   └── MessageRequest.java
│
├── error
│   ├── ErrorResponse.java
│   ├── MessageNotFoundException.java
│   └── PartnerNotFoundException.java
│
└── MessageRouterApplication.java
```

---

# Message API

Retrieve all messages

GET /api/messages

Retrieve paginated messages

GET /api/messages/paged?page=0&size=10&sortBy=timestamp&direction=desc

Retrieve message by ID

GET /api/message/{id}

Create message

POST /api/message

Delete message

DELETE /api/message/{id}

Send message

POST /api/message/send

Example request

```json
{
  "content": "Test message",
  "sender": "SYSTEM_A",
  "receiver": "SYSTEM_B"
}
```

---

# Partner API

Retrieve all partners

GET /api/partners

Retrieve paginated partners

GET /api/partners/paged

Retrieve partner by ID

GET /api/partners/{id}

Create partner

POST /api/partners

Delete partner

DELETE /api/partners/{id}

---

# Security

The application is secured using **OAuth2 with Keycloak**.

Role-based access:

**ADMIN**

* access message APIs

**USER**

* access partner APIs

Example header

Authorization: Bearer <access_token>

---

# IBM MQ Messaging

The application integrates with **IBM MQ** using **Spring JMS**.

### Message Producer

Send messages to the MQ queue using `MqService`:

sendMessage(String message)

### Message Consumer / Listener

Consume messages from the MQ queue using `MqMessageListener`:

```java
@Component
@Slf4j
public class MqMessageListener {
    public void handleMessage(String message) {
        log.info("Received message from MQ: {}", message);
    }
}
```

Benefits:

* asynchronous communication between systems
* reliable message delivery
* decoupled system architecture
* high scalability for banking systems

---

# Configuration

Sensitive configuration is not stored in Git.

The project includes:

application.yml.example

Developers must create their own configuration file.

Example configuration

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/message_router
    username: postgres
    password: password

mq:
  host: localhost
  port: 1414
  queue: MESSAGE.QUEUE

keycloak:
  realm: spring-app
  resource: spring-boot-client
```

---

# Running the Project

Build project

mvn clean install

Run application

mvn spring-boot:run

Application runs on

[http://localhost:8080](http://localhost:8080)

---

# Unit Testing

Unit tests cover:

* Service layer
* Controller layer

Technologies used:

* JUnit
* Mockito
* Spring Boot Test

Run tests

mvn test

---

# Purpose of the Project

This project simulates a **real enterprise banking backend system** integrating with messaging middleware.

It demonstrates:

* Spring Boot backend development
* IBM MQ messaging integration
* secure REST APIs
* OAuth2 authentication with Keycloak
* role-based authorization
* pagination and sorting
* exception handling
* backend testing
