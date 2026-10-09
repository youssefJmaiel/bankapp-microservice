# 🏦 BankApp Microservices

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.17-green)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-Gateway%20%7C%20Eureka-orange)
![Keycloak](https://img.shields.io/badge/Security-Keycloak%20%2F%20OAuth2-red)
![IBM MQ](https://img.shields.io/badge/Messaging-IBM%20MQ-blue)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
![Maven](https://img.shields.io/badge/Maven-3.8%2B-C71A36)
![Database](https://img.shields.io/badge/Database-H2%20%7C%20PostgreSQL--ready-lightgrey)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

> **Enterprise-oriented banking management platform built with Java, Spring Boot, Spring Cloud, OAuth2/JWT, Keycloak, IBM MQ and Docker, with a React frontend deployed on Cloudflare Pages.**

[🌐 **Live Demo**](https://cddd48c5.bankapp-frontend-606.pages.dev) · [💻 **Backend Repository**](https://github.com/youssefJmaiel/bankapp-microservice) · [⚛️ **Frontend Repository**](https://github.com/youssefJmaiel/bankapp-frontend)

> ℹ️ The frontend requires authentication through Keycloak. Demo credentials can be provided separately.

BankApp Microservices is a modular banking management application built around a **distributed microservices architecture**.

The project demonstrates how independent Spring Boot services can be **secured, discovered, routed and integrated** through REST APIs and asynchronous messaging with IBM MQ.

The architecture combines several enterprise Java concepts:

* Microservices
* API Gateway
* Service discovery
* OAuth2 / JWT authentication
* Role-based authorization
* REST APIs
* Asynchronous messaging
* Database persistence
* Docker containerization
* API documentation
* Unit testing

---

## 📸 Screenshots

The following screenshots show the main application features and authentication flow.

### 🔐 Authentication — Keycloak

![Keycloak Login](docs/screenshots/01-keycloak-login.png)

The application uses Keycloak to authenticate users and issue OAuth2/JWT access tokens.

---

### 🏦 Banking Dashboard

![BankApp Dashboard](docs/screenshots/02-dashboard.png)

The main dashboard provides access to the different business areas of the application.

---

### 👥 Employee Management

| Employees                                       | Add Employee                                          |
| ----------------------------------------------- | ----------------------------------------------------- |
| ![Employees](docs/screenshots/03-employees.png) | ![Add Employee](docs/screenshots/04-add-employee.png) |

The HR functionality allows users to view and manage employee information.

---

### 🏢 Department Management

| Departments                                         | Add Department                                            |
| --------------------------------------------------- | --------------------------------------------------------- |
| ![Departments](docs/screenshots/05-departments.png) | ![Add Department](docs/screenshots/06-add-department.png) |

Departments are managed through the HR service.

---

### 📨 Messaging

| Messages                                      | Send Banking Message                                                  |
| --------------------------------------------- | --------------------------------------------------------------------- |
| ![Messages](docs/screenshots/07-messages.png) | ![Send Banking Message](docs/screenshots/08-send-banking-message.png) |

Banking messages are handled by the Message Router and integrated with IBM MQ for asynchronous processing.

---

### 🤝 Partner Management

| Partners                                      | Add Partner                                         |
| --------------------------------------------- | --------------------------------------------------- |
| ![Partners](docs/screenshots/09-partners.png) | ![Add Partner](docs/screenshots/10-add-partner.png) |

The Message Router also exposes partner management functionality.

### Empty State

![Partners Empty State](docs/screenshots/11-partners-empty-state.png)

---

### 🎯 Mission Management

| Missions | Add Mission |
| -------- | ---------- |
| ![Missions](docs/screenshots/12-missions.png) | ![Add Mission](docs/screenshots/13-add-mission.png) |

Mission management is provided by the Mission Service.

---

# 🏗️ Architecture

```mermaid
flowchart TB

    CLIENT["React Frontend<br/>Cloudflare Pages"]

    KC["Keycloak<br/>OAuth2 / JWT<br/>Port 8081"]

    GW["Spring Cloud Gateway<br/>Port 8082"]

    EUREKA["Eureka Server<br/>Port 8761"]

    HR["HR Service<br/>Port 8083"]

    MISSION["Mission Service<br/>Port 8084"]

    MSG["Message Router<br/>Port 8085"]

    NOTIF["Notification Service<br/>Port 8086"]
    MAILPIT["Mailpit SMTP / Web UI<br/>Ports 1025 / 8025"]

    HRDB[("H2 / PostgreSQL")]
    MISSIONDB[("H2 / PostgreSQL")]
    MSGDB[("H2 / PostgreSQL")]

    MQ["IBM MQ<br/>QM1<br/>Port 1414"]

    CLIENT -->|"Authentication"| KC
    KC -->|"JWT"| CLIENT

    CLIENT -->|"Bearer JWT"| GW

    GW -->|"Service Discovery"| EUREKA

    EUREKA -.-> HR
    EUREKA -.-> MISSION
    EUREKA -.-> MSG
    EUREKA -.-> NOTIF

    GW --> HR
    GW --> MISSION
    GW --> MSG

    HR --> HRDB
    MISSION --> MISSIONDB
    MSG --> MSGDB
    NOTIF -->|"SMTP email delivery"| MAILPIT

    MSG -->|"Asynchronous Messaging"| MQ
    MQ -->|"Message Consumption"| MSG
```

---

## 🔄 End-to-End Request Flow

```text
React Frontend
      │
      ▼
  Keycloak
      │
      │ JWT
      ▼
Spring Cloud Gateway
      │
      ▼
   Eureka
      │
 ┌────┼───────────────┐
 ▼    ▼               ▼
 HR  Mission    Message Router
                         │
                         ▼
                      IBM MQ
```

### Authentication flow

```text
User
  ↓
Keycloak
  ↓
JWT Access Token
  ↓
React Frontend
  ↓
Spring Cloud Gateway
  ↓
Downstream Microservice
```

### Banking message flow

```text
React
  ↓
Gateway
  ↓
Message Router
  ↓
Database
  ↓
IBM MQ
  ↓
MQ Listener
  ↓
Message Processing
```

---

# 🚀 Project Highlights

* 🧩 Modular **microservices architecture**
* ☕ **Java 17** backend
* 🌱 **Spring Boot 2.7.17**
* 🌐 **Spring Cloud Gateway**
* 🔎 **Netflix Eureka service discovery**
* 🔐 **Keycloak + OAuth2 / JWT**
* 👥 Role-based authorization with `ADMIN`, `AUDITOR` and `USER`
* 👤 Employee provisioning and activation through Keycloak
* 📬 HTML email notification service with Thymeleaf and SMTP
* 📨 **IBM MQ** asynchronous messaging
* 🐳 Docker and Docker Compose
* 🗄️ H2 database for development
* 🐘 PostgreSQL-ready configuration
* 📚 Swagger / OpenAPI documentation
* 🧪 Unit testing
* ⚙️ Environment-based configuration
* ⚛️ React + TypeScript frontend
* ☁️ Frontend deployment using Cloudflare Pages

---

# 🧾 Technology Stack

| Layer             | Technology              | Version / Details              |
| ----------------- | ----------------------- | ------------------------------ |
| Language          | Java                    | 17                             |
| Framework         | Spring Boot             | 2.7.17                         |
| Cloud             | Spring Cloud            | Gateway / Eureka               |
| Service Discovery | Netflix Eureka          | —                              |
| Security          | Keycloak                | OAuth2 / JWT                   |
| Authentication    | OAuth2 Resource Server  | JWT                            |
| Messaging         | IBM MQ                  | QM1                            |
| Email             | JavaMailSender / SMTP   | Thymeleaf HTML templates       |
| Email testing     | Mailpit                 | SMTP 1025 / Web UI 8025        |
| Database          | H2                      | Development                    |
| Database          | PostgreSQL              | Production-ready configuration |
| Build             | Maven                   | 3.8+                           |
| Containers        | Docker / Docker Compose | —                              |
| Frontend          | React / TypeScript      | —                              |
| API Documentation | Swagger / OpenAPI       | —                              |
| Deployment        | Cloudflare Pages        | Frontend                       |

---

# 📦 Microservices

| Service             |   Port | Responsibility                                |
| ------------------- | -----: | --------------------------------------------- |
| **API Gateway**     | `8082` | Central API entry point, routing and security |
| **Auth Service**    |      — | Authentication-related backend integration    |
| **HR Service**      | `8083` | Employees and departments                     |
| **Mission Service** | `8084` | Mission management                            |
| **Message Router**  | `8085` | Messages, partners and IBM MQ integration     |
| **Notification Service** | `8086` | HTML email rendering and SMTP delivery |
| **Eureka Server**   | `8761` | Service discovery                             |
| **IBM MQ**          | `1414` | Asynchronous enterprise messaging             |
| **Keycloak**        | `8081` | Identity and access management                |

---

# 🌐 API Gateway

The Spring Cloud Gateway provides a centralized entry point for frontend requests.

### Gateway routes

```text
/api/employees/**   → HR-SERVICE
/api/departments/** → HR-SERVICE
/api/missions/**    → MISSION-SERVICE
/api/messages/**    → MESSAGE-ROUTER
/api/message/**     → MESSAGE-ROUTER
/api/partners/**    → PARTNER-SERVICE
```

The Gateway uses Eureka service discovery and Spring Cloud LoadBalancer.

```text
lb://HR-SERVICE
lb://MISSION-SERVICE
lb://MESSAGE-ROUTER
lb://PARTNER-SERVICE
```

This allows the Gateway to route requests using logical service names rather than hard-coded service IP addresses.

---

# 🔐 Security

Authentication and authorization are implemented with **Keycloak, OAuth2 and JWT**.

### Keycloak configuration

```text
Realm:  spring-app
Client: spring-boot-client
Roles:  ADMIN
        AUDITOR
        USER
```

Authenticated requests use:

```http
Authorization: Bearer <JWT_TOKEN>
```

### Security architecture

```text
React Frontend
      │
      │ Authentication
      ▼
  Keycloak
      │
      │ JWT
      ▼
Spring Cloud Gateway
      │
      ▼
Downstream Services
```

The Gateway validates incoming JWT tokens before routing requests to protected backend services.

The downstream services also use Spring Security for resource-server authentication.

---

## 🛡️ Security Validation

The authentication flow has been tested using Keycloak-issued JWT tokens.

| Scenario                 | Result             |
| ------------------------ | ------------------ |
| Valid JWT                | `200 OK`           |
| Missing JWT              | `401 Unauthorized` |
| JWT-based authentication | Validated          |
| `ADMIN` role             | Supported          |
| `AUDITOR` role           | Supported          |
| `USER` role              | Supported          |
| Gateway → HR with JWT    | `200 OK`           |

No credentials, access tokens or secrets are stored in the repository.

---

# 👤 Employee Account Creation & Activation

Employee creation in the HR service is connected to Keycloak account provisioning.

## Workflow

1. An administrator submits an employee creation request through the HR functionality.
2. `EmployeeService` converts the request and saves the employee record.
3. `KeycloakAdminService` obtains an administrative access token using the configured Keycloak client credentials.
4. A Keycloak user is created using the employee's email address as the username, with the employee's name and email details.
5. The new account is enabled, the email is initially unverified, and the `USER` realm role is assigned.
6. Keycloak is asked to send an activation email with the required actions `VERIFY_EMAIL` and `UPDATE_PASSWORD`.
7. The activation link is configured to expire after 24 hours.
8. The employee verifies the email address and sets a password before signing in.

If provisioning fails after a Keycloak user has been created, the service attempts to remove that Keycloak user and reports the error.

> **Email distinction:** this activation email is sent through Keycloak. It is separate from the standalone BankApp Notification microservice documented below; automatic invocation of that notification service during employee creation has not been confirmed.

---

# 📨 IBM MQ — Asynchronous Messaging

The Message Router integrates IBM MQ to support asynchronous message processing.

```text
REST Client
     │
     ▼
API Gateway
     │
     ▼
Message Router
     │
     ├── Database
     │
     ▼
  IBM MQ
     │
     ├── DEV.QUEUE.1
     └── DEV.QUEUE.2
```

The Message Router contains components responsible for:

* MQ connection configuration
* Message production
* Message consumption
* Queue listeners
* JSON serialization/deserialization
* Message routing
* Exception handling
* Message persistence

This demonstrates the combination of:

```text
Synchronous REST APIs
          +
Asynchronous IBM MQ messaging
```

---

# 📬 Notification Microservice

BankApp contains a dedicated notification service named `bankapp-notification` for rendering and sending HTML emails.

| Property | Value |
|---|---|
| Service | `bankapp-notification` |
| Default port | `8086` |
| Template engine | Thymeleaf |
| Email delivery | Spring `JavaMailSender` over SMTP |
| Local SMTP server | Mailpit on port `1025` |
| Mailpit web interface | `http://localhost:8025` |

## Email processing

1. The service receives a validated mail request.
2. It loads the named Thymeleaf template and adds the supplied variables to the template context.
3. Thymeleaf renders the HTML content.
4. `JavaMailSender` sends the email through the configured SMTP server.

The included template is `bankapp-notification/src/main/resources/templates/employee-created.html` and supports variables such as `firstName`, `lastName` and `email`.

## Send email API

**Endpoint:** `POST http://localhost:8086/api/notifications/send`

Example request body:

```json
{
  "to": "employee@example.com",
  "subject": "Welcome to BankApp",
  "template": "employee-created",
  "locale": "en",
  "variables": {
    "firstName": "John",
    "lastName": "Doe",
    "email": "employee@example.com"
  }
}
```

The `to` field must contain a valid email address. `to`, `subject` and `template` are required. `variables` supplies the values used by the template.

## Local email testing

When the development Mailpit container is running, inspect captured messages at `http://localhost:8025`. The service's default local SMTP configuration uses `localhost:1025`; set `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME` and `MAIL_PASSWORD` to match the environment. SMTP authentication and STARTTLS are disabled by default in the supplied local configuration and should be configured appropriately for a real mail provider.

The service uses the configured Eureka URL through `EUREKA_URL` (default `http://localhost:8761/eureka/`). The inspected Gateway configuration does not define a `/api/notifications/**` route, so the documented endpoint is accessed directly on port `8086` unless a Gateway route is added.

> **Integration note:** the employee activation email currently documented in the HR flow is sent by Keycloak. The existence of the notification service and its `employee-created.html` template does not by itself mean that HR automatically invokes it.

---

# 🔎 Service Discovery — Eureka

Eureka provides service registration and discovery.

```text
                    Eureka
                    :8761
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
      HR Service    Mission     Message Router
        :8083       Service         :8085
                     :8084
```

The Gateway communicates with services through logical service identifiers:

```text
HR-SERVICE
MISSION-SERVICE
MESSAGE-ROUTER
```

This avoids hard-coded backend service addresses in the Gateway routing configuration.

---

# 🗄️ Data Persistence

The project currently uses H2 for development and is prepared for PostgreSQL-based deployment.

| Environment              | Database         |
| ------------------------ | ---------------- |
| Development              | H2               |
| Production configuration | PostgreSQL-ready |

The Message Router persists domain information including:

* Messages
* Partners

The HR and Mission services also maintain their respective domain data.

---

# 🐳 Docker & Docker Compose

The main backend components are containerized using Docker.

Example Dockerfiles:

```text
auth-service/Dockerfile
discovery-server/Dockerfile
gateway-service/Dockerfile
hr-service/Dockerfile
message-router/Dockerfile
mission-service/Dockerfile
```

The complete environment can be orchestrated with:

```text
docker-compose.yml
```

### Environment variables

Sensitive configuration is provided through environment variables:

```text
KEYCLOAK_CLIENT_SECRET
MQ_USER_IBM
MQ_PASSWORD_IBM
MQ_APP_PASSWORD
```

The real `.env` file is excluded from Git using `.gitignore`.

An example configuration is provided through:

```text
.env.example
```

Example:

```env
KEYCLOAK_CLIENT_SECRET=your_keycloak_client_secret
MQ_USER_IBM=your_mq_user
MQ_PASSWORD_IBM=your_mq_password
MQ_APP_PASSWORD=your_mq_app_password
```

> **Important:** Never commit the real `.env` file or any credentials to the repository.

---

# ⚙️ Running the Project

## Prerequisites

Install:

* Java 17
* Maven 3.8+
* Git
* Docker
* Docker Compose

---

## 1. Clone the repository

```bash
git clone https://github.com/youssefJmaiel/bankapp-microservice.git
cd bankapp-microservice
```

---

## 2. Configure environment variables

Create the local environment file:

```bash
cp .env.example .env
```

Then configure the required values:

```env
KEYCLOAK_CLIENT_SECRET=your_keycloak_client_secret
MQ_USER_IBM=your_mq_user
MQ_PASSWORD_IBM=your_mq_password
MQ_APP_PASSWORD=your_mq_app_password
```

---

## 3. Configure Keycloak

Create the following Keycloak configuration:

```text
Realm:
spring-app

Client:
spring-boot-client

Roles:
ADMIN
AUDITOR
USER
```

Configure the client according to the authentication flow used by the application.

---

## 4. Start the backend environment

```bash
docker compose up --build
```

For older Docker installations:

```bash
docker-compose up --build
```

---

## 5. Verify the containers

```bash
docker compose ps
```

Expected backend components include:

```text
GATEWAY-SERVICE
HR-SERVICE
MISSION-SERVICE
MESSAGE-ROUTER
DISCOVERY-SERVER
```

Keycloak and IBM MQ are also required for the complete environment.

---

## 6. Verify Eureka

Open:

```text
http://localhost:8761
```

The Gateway should be able to discover the registered backend services.

---

## 7. Verify Gateway security

A protected endpoint without authentication should return:

```text
401 Unauthorized
```

Example:

```bash
curl -i http://localhost:8082/api/employees
```

---

# 🌐 Backend Endpoints

| Component       | URL                                           |
| --------------- | --------------------------------------------- |
| API Gateway     | `http://localhost:8082`                       |
| HR Service      | `http://localhost:8083/api/employees`         |
| Mission Service | `http://localhost:8084/api/missions`          |
| Message Router  | `http://localhost:8085/api/messages`          |
| Notification Service | `http://localhost:8086/api/notifications/send` |
| Mailpit Web UI  | `http://localhost:8025`                       |
| Partners        | `http://localhost:8085/api/partners`          |
| Eureka          | `http://localhost:8761`                       |
| Keycloak        | `http://localhost:8081`                       |
| Swagger UI      | `http://localhost:8083/swagger-ui/index.html` |

For normal frontend communication, requests are intended to go through the **API Gateway**.

---

# 📚 API Documentation

Swagger / OpenAPI is used to document backend REST APIs.

Example:

```text
http://localhost:8083/swagger-ui/index.html
```

Other services expose their own API documentation depending on their configuration.

---

# 🧪 Testing

The project includes unit tests for business logic.

Run the Maven test suite with:

```bash
./mvnw test
```

or:

```bash
mvn test
```

Example test location:

```text
message-router/src/test/
```

Testing focuses on service-layer business logic and application behavior.

---

# 📁 Project Structure

```text
bankapp-microservice/
│
├── auth-service/
├── bankapp-platform/
├── bankapp-domain/
│   ├── mission-domain/
│   ├── notification-domain/
│   ├── hr-domain/
│   ├── partner-domain/
│   └── message-domain/
├── bankapp-notification/
├── career-service/
├── common/
├── config-server/
├── discovery-server/
├── gateway-service/
├── hr-service/
├── message-router/
├── mission-service/
├── partner-service/
│
├── docs/
│   └── screenshots/
│       ├── 01-keycloak-login.png
│       ├── 02-dashboard.png
│       ├── 03-employees.png
│       ├── 04-add-employee.png
│       ├── 05-departments.png
│       ├── 06-add-department.png
│       ├── 07-messages.png
│       ├── 08-send-banking-message.png
│       ├── 09-partners.png
│       ├── 10-add-partner.png
│       └── 11-partners-empty-state.png
│
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

---

# 🔄 Main Technical Concepts Demonstrated

This project was developed to practice and demonstrate the following backend and distributed-system concepts:

### Microservices

Independent services with separated business responsibilities.

### API Gateway

Centralized routing between the frontend and backend services.

### Service Discovery

Dynamic service registration and discovery using Eureka.

### OAuth2 / JWT

Token-based authentication and authorization.

### Keycloak

Centralized identity and access management.

### Role-Based Authorization

Application roles:

```text
ADMIN
AUDITOR
USER
```

### REST APIs

Communication between the frontend, Gateway and backend services.

### Asynchronous Messaging

IBM MQ for asynchronous enterprise message processing.

### Persistence

H2 for development with PostgreSQL-ready configuration.

### Containerization

Docker and Docker Compose for service deployment and infrastructure management.

### API Documentation

Swagger / OpenAPI for REST API documentation.

### Automated Testing

Unit tests for service-layer business logic.

---

# 🎯 Project Objectives

The main objective of BankApp Microservices is to implement and demonstrate a distributed backend using technologies commonly used in **enterprise Java development**.

The project focuses on:

* Separation of business responsibilities
* Secure API communication
* Centralized authentication
* Role-based authorization
* API Gateway routing
* Dynamic service discovery
* REST-based service communication
* Asynchronous messaging
* Database persistence
* Containerized infrastructure
* Maintainable service structure
* API documentation
* Unit testing
* Development-to-production database flexibility

---

# 💼 What This Project Demonstrates

From a software engineering perspective, the project demonstrates practical experience with:

```text
Java
  │
  ├── Spring Boot
  ├── Spring Security
  ├── Spring Cloud
  │      ├── Gateway
  │      └── Eureka
  │
  ├── OAuth2 / JWT
  ├── Keycloak
  ├── REST APIs
  ├── IBM MQ
  ├── H2 / PostgreSQL
  ├── Docker
  ├── Maven
  └── Swagger / OpenAPI
```

The frontend complements the backend architecture with:

```text
React
TypeScript
REST API integration
Keycloak authentication
Cloudflare Pages deployment
```

---

# 🌐 Live Application

The React frontend is available online:

**https://cddd48c5.bankapp-frontend-606.pages.dev**

The frontend communicates with the backend through the configured API Gateway.

Authentication is handled through Keycloak.

> The live environment may depend on the availability of the backend infrastructure and authentication configuration.

---

# 👨‍💻 Author

**Youssef Jmaiel**

Computer Science Engineer focused on:

**Java Backend · Spring Boot · Microservices**

### GitHub

https://github.com/youssefJmaiel

### Project

https://github.com/youssefJmaiel/bankapp-microservice

---

# 📜 License

This project is licensed under the **MIT License**.
