# 🏦 BankApp Microservices

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.17-green)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2021.0.8-orange)
![Keycloak](https://img.shields.io/badge/Security-Keycloak%20%2F%20OAuth2-red)
![IBM MQ](https://img.shields.io/badge/Messaging-IBM%20MQ-blue)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-336791)
![Liquibase](https://img.shields.io/badge/Migrations-Liquibase-2962FF)
![React](https://img.shields.io/badge/Frontend-React-61DAFB)
![TypeScript](https://img.shields.io/badge/Language-TypeScript-3178C6)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
![Maven](https://img.shields.io/badge/Maven-3.8%2B-C71A36)
![Cloudflare](https://img.shields.io/badge/Deployment-Cloudflare%20Pages-F38020)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

> **Enterprise-oriented banking management platform built with Java 17, Spring Boot, Spring Cloud, Keycloak, OAuth2/JWT, PostgreSQL, Liquibase, IBM MQ, Docker, React and TypeScript.**

🌐 **Live Demo:**
https://cddd48c5.bankapp-frontend-606.pages.dev

💻 **Backend Repository:**
https://github.com/youssefJmaiel/bankapp-microservice

💻 **Frontend Repository:**
https://github.com/youssefJmaiel/bankapp-frontend

---

# 📌 Overview

**BankApp Microservices** is a full-stack banking management platform designed around a distributed **microservices architecture**.

The backend is built with **Java 17, Spring Boot and Spring Cloud**, while the frontend is implemented with **React and TypeScript**.

The platform demonstrates how independent business services can be:

* secured with Keycloak and OAuth2/JWT;
* discovered dynamically through Eureka;
* exposed through a centralized API Gateway;
* persisted using PostgreSQL;
* versioned with Liquibase;
* integrated with IBM MQ for asynchronous messaging;
* containerized with Docker;
* consumed by a modern React frontend.

The project combines **synchronous REST communication** with **asynchronous enterprise messaging**.

---

# ✨ Main Features

* 🔐 Keycloak authentication
* 🪪 OAuth2 / JWT security
* 👑 Role-Based Access Control
* 👥 Employee management
* 🏢 Department management
* 🎯 Mission management
* 🤝 Partner management
* 📨 Banking message management
* 📬 Personal user message inbox
* 📨 IBM MQ asynchronous messaging
* 🌐 Spring Cloud Gateway
* 🔎 Eureka service discovery
* 🗄️ PostgreSQL persistence
* 🔄 Liquibase migrations
* 🐳 Docker / Docker Compose
* 📚 Swagger / OpenAPI
* 🧪 Unit and integration-oriented testing
* ⚛️ React + TypeScript frontend
* ☁️ Cloudflare Pages deployment

---

# 🏗️ System Architecture

```mermaid
flowchart TB

    USER["User"]

    FRONT["React + TypeScript Frontend"]

    KC["Keycloak<br/>OAuth2 / JWT<br/>:8081"]

    GW["Spring Cloud Gateway<br/>:8082"]

    EUREKA["Eureka Discovery Server<br/>:8761"]

    HR["HR Service<br/>:8083"]
    MISSION["Mission Service<br/>:8084"]
    MSG["Message Router<br/>:8085"]
    PARTNER["Partner Service<br/>:8087"]

    HRDB[("PostgreSQL<br/>hr_schema")]
    MISSIONDB[("PostgreSQL<br/>mission_schema")]
    MSGDB[("PostgreSQL<br/>message_schema")]
    PARTNERDB[("PostgreSQL<br/>partner_schema")]

    MQ["IBM MQ<br/>QM1<br/>:1414"]

    USER --> FRONT
    FRONT -->|"Authentication"| KC
    KC -->|"JWT"| FRONT

    FRONT -->|"Bearer JWT"| GW

    GW --> EUREKA

    EUREKA -.-> HR
    EUREKA -.-> MISSION
    EUREKA -.-> MSG
    EUREKA -.-> PARTNER

    GW -->|"lb://HR-SERVICE"| HR
    GW -->|"lb://MISSION-SERVICE"| MISSION
    GW -->|"lb://MESSAGE-ROUTER"| MSG
    GW -->|"lb://PARTNER-SERVICE"| PARTNER

    HR --> HRDB
    MISSION --> MISSIONDB
    MSG --> MSGDB
    PARTNER --> PARTNERDB

    MSG -->|"Asynchronous Messaging"| MQ
    MQ -->|"Message Consumption"| MSG

    Notification[bankapp-notification :8086]
    SMTP[Configurable SMTP provider<br/>Gmail or another provider]
    Mailpit[Optional local Mailpit<br/>SMTP :1025 / Web UI :8025]
    Notification -->|Service registration| Eureka
    Notification -->|Configured SMTP| SMTP
    Notification -. Optional local testing .-> Mailpit
```

---

# 🔄 End-to-End Request Flow

The frontend uses the **API Gateway as the central backend entry point**.

```text
                         ┌──────────────┐
                         │   Keycloak   │
                         │    :8081     │
                         └──────┬───────┘
                                │
                              JWT
                                │
                                ▼
┌────────────────┐      ┌────────────────┐
│ React Frontend │─────▶│ API Gateway    │
│   TypeScript   │      │     :8082      │
└────────────────┘      └───────┬────────┘
                                │
                                ▼
                         ┌──────────────┐
                         │    Eureka    │
                         │    :8761     │
                         └──────┬───────┘
                                │
          ┌─────────────────────┼─────────────────────┐
          │                     │                     │
          ▼                     ▼                     ▼
     HR Service          Mission Service       Message Router
       :8083                  :8084                 :8085
          │                     │                     │
          │                     │                     ├── PostgreSQL
          │                     │                     │
          │                     │                     └── IBM MQ
          │                     │
          │                     ▼
          │                 PostgreSQL
          │
          ▼
      PostgreSQL

                                │
                                ▼
                         Partner Service
                              :8087
                                │
                                ▼
                           PostgreSQL
```

---

# 🔐 Authentication & Security

The application uses **Keycloak** as the centralized identity and access management system.

Authentication is based on:

```text
Keycloak
   │
   ▼
OAuth2
   │
   ▼
JWT Access Token
   │
   ▼
Spring Security
   │
   ▼
Protected Microservice
```

Authenticated API requests contain:

```http
Authorization: Bearer <JWT_TOKEN>
```

Each protected backend service validates the JWT using its configured Keycloak issuer/JWK configuration.

The backend remains responsible for enforcing authorization.

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

# 👑 Role-Based Access Control

The main application roles are:

```text
ADMIN
USER
```

The project follows a defense-in-depth approach:

```text
Frontend role-based visibility
            +
Backend Spring Security
            +
Keycloak JWT
            =
Protected application
```

Frontend restrictions improve the user experience, but they are **not considered a security boundary**.

The backend independently validates permissions.

---

# 👑 ADMIN Permissions

The administrator has management access to the main business modules.

### Employees

* View employees
* Create employees
* Update employees
* Delete employees

### Departments

* View departments
* Create departments
* Update departments
* Delete departments

### Missions

* View missions
* Create missions
* Update missions
* Delete missions
* Assign missions to employees

### Partners

* View partners
* Create partners
* Delete partners

### Messages

* View banking messages
* Send banking messages
* Delete messages
* Use the messaging workflow integrated with IBM MQ

---

# 👤 USER Permissions

Standard users have consultation and personal-access permissions.

Users can:

* View employees
* View departments
* View missions
* View partners
* Access their personal message inbox
* Read messages addressed to their authenticated username

Users cannot:

* Create employees
* Update employees
* Delete employees
* Create departments
* Update departments
* Delete departments
* Create missions
* Update missions
* Delete missions
* Create partners
* Delete partners
* Send banking messages
* Delete messages
* Access administrative message collections

---

# 📨 Personal Message Inbox

The application provides a dedicated personal messaging endpoint:

```http
GET /api/messages/my
```

The Message Router extracts the authenticated user's username from the JWT:

```text
preferred_username
```

The backend then returns only messages addressed to that user.

Example:

```text
JWT
 │
 └── preferred_username = john
                │
                ▼
       GET /api/messages/my
                │
                ▼
       receiver = "john"
                │
                ▼
        Personal messages
```

This prevents standard users from accessing the complete administrative message collection.

---

# 🤝 Partner Management

Partner management is implemented as an **independent microservice**.

```text
Partner Service
      │
      ├── Spring Boot
      ├── Spring Security
      ├── JWT / Keycloak
      ├── Eureka
      ├── PostgreSQL
      └── Liquibase
```

The Partner Service runs on:

```text
8087
```

Its API is:

```text
/api/partners/**
```

The frontend does **not** call port `8087` directly.

Instead:

```text
React Frontend
      │
      ▼
Gateway :8082
      │
      ▼
Eureka
      │
      ▼
PARTNER-SERVICE :8087
```

This keeps Partner consistent with the rest of the microservice architecture.

---

# 🌐 API Gateway

Spring Cloud Gateway provides the central entry point for frontend API requests.

## Gateway Routes

| Frontend API        | Destination       |
| ------------------- | ----------------- |
| `/api/employees/**` | `HR-SERVICE`      |
| `/api/missions/**`  | `MISSION-SERVICE` |
| `/api/messages/**`  | `MESSAGE-ROUTER`  |
| `/api/partners/**`  | `PARTNER-SERVICE` |

The Gateway uses Eureka service discovery and logical service names:

```text
lb://HR-SERVICE
lb://MISSION-SERVICE
lb://MESSAGE-ROUTER
lb://PARTNER-SERVICE
```

Therefore, the frontend does not need to know the internal service addresses.

---

# 🔎 Eureka Service Discovery

The Discovery Server uses **Netflix Eureka** to register and discover microservices dynamically.

```text
                     Eureka :8761
                           │
        ┌──────────────────┼───────────────────┐
        │                  │                   │
        ▼                  ▼                   ▼
   HR-SERVICE       MISSION-SERVICE     MESSAGE-ROUTER
      :8083              :8084               :8085

                           │
                           ▼
                    PARTNER-SERVICE
                         :8087
```

This allows the Gateway to route requests without hard-coding individual container IP addresses.

---


**Notification service:** `bankapp-notification` runs on port `8086` and exposes its notification endpoint directly. It uses SMTP for email delivery and Mailpit for local testing. The inspected Gateway configuration does not currently define a `/api/notifications/**` route, and automatic invocation from HR has not been confirmed.

# 🧩 Microservices

| Service              |   Port | Responsibility                  |
| -------------------- | -----: | ------------------------------- |
| **Gateway Service**  | `8082` | Central API Gateway and routing |
| **HR Service**       | `8083` | Employees and departments       |
| **Mission Service**  | `8084` | Mission management              |
| **Notification Service** | `8086` | Email notifications via SMTP and Thymeleaf |
| **Message Router**   | `8085` | Banking messages and IBM MQ     |
| **Partner Service**  | `8087` | Partner management              |
| **Discovery Server** | `8761` | Eureka service discovery        |
| **Keycloak**         | `8081` | Authentication and identity     |
| **IBM MQ**           | `1414` | Asynchronous messaging          |

Additional project modules are present in the repository for shared infrastructure and application components.

---

# 🗂️ Service Responsibilities

## 👥 HR Service

The HR Service manages:

```text
Employees
Departments
```

Database schema:

```text
hr_schema
```

Main responsibilities:

* Employee management
* Department management
* Validation
* REST APIs
* JWT authorization
* PostgreSQL persistence
* Liquibase migrations

---

## 🎯 Mission Service

The Mission Service manages:

```text
Missions
Employee assignments
```

Database schema:

```text
mission_schema
```

Mission assignments use an employee identifier:

```text
assignedEmployeeId
```

The Mission Service does not maintain a direct database foreign key to the HR Service.

This preserves the independence of the microservices.

---

## 📨 Message Router

The Message Router is responsible for:

```text
Banking Messages
IBM MQ integration
Message production
Message consumption
```

Database schema:

```text
message_schema
```

The Message Router combines:

```text
REST API
    +
PostgreSQL
    +
IBM MQ
```

---

## 🤝 Partner Service

The Partner Service is responsible exclusively for:

```text
Partner Management
```

Database schema:

```text
partner_schema
```

The service provides:

* Partner consultation
* Paginated partner consultation
* Partner details
* Partner creation
* Partner deletion
* JWT authorization
* Role-based access control
* PostgreSQL persistence
* Liquibase migrations
* Eureka registration

Partner APIs:

```text
GET    /api/partners
GET    /api/partners/paged
GET    /api/partners/{id}
POST   /api/partners
DELETE /api/partners/{id}
```

---

# 📨 IBM MQ — Asynchronous Messaging

The Message Router integrates **IBM MQ** to demonstrate enterprise asynchronous messaging.

```text
React Frontend
      │
      ▼
API Gateway
      │
      ▼
Message Router :8085
      │
      ├───────────────▶ PostgreSQL
      │
      ▼
    IBM MQ
      │
      ▼
   MQ Listener
      │
      ▼
Message Processing
```

The IBM MQ integration contains components for:

* MQ connection configuration
* Message production
* Message consumption
* Queue listeners
* JSON serialization/deserialization
* Message routing
* Exception handling
* Message persistence

The project demonstrates:

```text
Synchronous REST APIs
          +
Asynchronous IBM MQ Messaging
```

---

# 📬 Message Processing

## ADMIN Message Flow

```text
ADMIN
  │
  ▼
React Frontend
  │
  ▼
API Gateway
  │
  ▼
Message Router
  │
  ├── Persist message
  │
  ▼
IBM MQ
  │
  ▼
MQ Listener
  │
  ▼
Message processing
```

## USER Personal Inbox

```text
USER
 │
 ▼
React Frontend
 │
 ▼
API Gateway
 │
 ▼
Message Router
 │
 ▼
/api/messages/my
 │
 ▼
JWT preferred_username
 │
 ▼
Messages addressed to USER
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
| SMTP configuration | Gmail by default in Docker Compose (`smtp.gmail.com:587`, STARTTLS) |
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

Docker Compose defaults to Gmail SMTP (`smtp.gmail.com:587`) with authentication and STARTTLS enabled. Set `MAIL_USERNAME` and `MAIL_PASSWORD` in your local `.env` file; for Gmail, use an app password rather than your normal account password. Never commit real credentials. For local email testing, configure `MAIL_HOST=mailpit`, `MAIL_PORT=1025`, `MAIL_SMTP_AUTH=false` and `MAIL_SMTP_STARTTLS=false`, then inspect captured messages at `http://localhost:8025`. For another provider, adjust the SMTP variables to match its requirements.

The service uses the configured Eureka URL through `EUREKA_URL` (default `http://localhost:8761/eureka/`). The inspected Gateway configuration does not define a `/api/notifications/**` route, so the documented endpoint is accessed directly on port `8086` unless a Gateway route is added.

> **Integration note:** the employee activation email currently documented in the HR flow is sent by Keycloak. The existence of the notification service and its `employee-created.html` template does not by itself mean that HR automatically invokes it.

---

# 🗄️ Database Architecture

The application uses **PostgreSQL** as the primary relational database.

The database is organized using dedicated schemas according to service ownership.

```text
PostgreSQL
│
└── bankapp
    │
    ├── hr_schema
    │   ├── employees
    │   ├── departments
    │   └── Liquibase metadata
    │
    ├── mission_schema
    │   ├── missions
    │   └── Liquibase metadata
    │
    ├── message_schema
    │   ├── messages
    │   └── Liquibase metadata
    │
    └── partner_schema
        ├── partners
        └── Liquibase metadata
```

## Service Ownership

| Service         | Schema           | Main Data              |
| --------------- | ---------------- | ---------------------- |
| HR Service      | `hr_schema`      | Employees, Departments |
| Mission Service | `mission_schema` | Missions               |
| Message Router  | `message_schema` | Messages               |
| Partner Service | `partner_schema` | Partners               |

This provides logical data isolation while using a shared PostgreSQL database instance.

---

# 🔄 Liquibase Database Migrations

Database schema creation and evolution are managed with **Liquibase**.

Each service owns its own migration history.

```text
HR Service
└── Liquibase
    └── hr_schema

Mission Service
└── Liquibase
    └── mission_schema

Message Router
└── Liquibase
    └── message_schema

Partner Service
└── Liquibase
    └── partner_schema
```

Hibernate is configured for schema validation rather than automatic schema modification:

```yaml
ddl-auto: validate
```

Therefore:

```text
Liquibase
    │
    ├── Creates schema
    ├── Creates tables
    └── Evolves database

Hibernate
    │
    └── Validates schema
```

This approach provides:

* Version-controlled database changes
* Reproducible database initialization
* Controlled schema evolution
* Clear migration history
* Separation between ORM and database migration responsibilities

---

# 🐳 Docker & Docker Compose

The backend infrastructure is containerized using Docker and Docker Compose.

The repository contains:

```text
docker-compose.yml
```

The main containerized components include:

```text
auth-service
discovery-server
gateway-service
hr-service
message-router
mission-service
partner-service
ibm-mq
```

PostgreSQL is currently provided by the host environment.

Application containers connect to the host PostgreSQL instance through:

```text
host.docker.internal
```

This keeps PostgreSQL separate from the Docker Compose application stack.

---

# ⚙️ Environment Configuration

Sensitive configuration is provided through environment variables.

Example:

```env
KEYCLOAK_CLIENT_SECRET=your_keycloak_client_secret

MQ_USER_IBM=your_mq_user
MQ_PASSWORD_IBM=your_mq_password
MQ_APP_PASSWORD=your_mq_app_password

POSTGRES_DB=bankapp
POSTGRES_USER=bankapp
POSTGRES_PASSWORD=your_postgres_password
```

The real `.env` file must never be committed.

Recommended `.gitignore` entries:

```gitignore
.env
target/
```

The repository provides:

```text
.env.example
```

> ⚠️ **Never commit passwords, client secrets, access tokens or other credentials to GitHub.**

---

# 🚀 Running the Backend

## Prerequisites

Install:

* Java 17
* Maven 3.8+
* Docker
* Docker Compose
* Git
* PostgreSQL

---

## 1. Clone the Repository

```bash
git clone https://github.com/youssefJmaiel/bankapp-microservice.git
cd bankapp-microservice
```

---

## 2. Configure Environment Variables

Create the local environment file:

```bash
cp .env.example .env
```

Then configure the required credentials.

Do not commit the resulting `.env` file.

---

## 3. Build the Shared Domain Modules

Before starting Docker, build the shared modules and the message-router JAR:

```bash
cd bankapp-domain && mvn install -DskipTests
cd ../message-router
cp src/main/resources/application.yaml.example src/main/resources/application.yaml
mvn clean package -DskipTests
cd ..
```

Configure IBM MQ credentials in the root `.env` file. Never commit real credentials.

---

## 3. Start the Backend

```bash
docker compose up --build
```

To start in detached mode:

```bash
docker compose up -d --build
```

---

## 4. Check Containers

```bash
docker compose ps
```

The expected application stack includes:

```text
bankapp-auth
bankapp-discovery
bankapp-gateway
bankapp-hr
bankapp-message-router
bankapp-mission
bankapp-partner
bankapp-ibm-mq
```

Container names may vary depending on the Compose configuration.

---

# 🔎 Verify Eureka

Open:

```text
http://localhost:8761
```

Expected registered services include:

```text
AUTH-SERVICE
GATEWAY-SERVICE
HR-SERVICE
MISSION-SERVICE
MESSAGE-ROUTER
PARTNER-SERVICE
```

---

# 🌐 Backend Endpoints

| Component       | URL                     |
| --------------- | ----------------------- |
| Keycloak        | `http://localhost:8081` |
| API Gateway     | `http://localhost:8082` |
| HR Service      | `http://localhost:8083` |
| Mission Service | `http://localhost:8084` |
| Message Router  | `http://localhost:8085` |
| Partner Service | `http://localhost:8087` |
| Eureka          | `http://localhost:8761` |
| IBM MQ          | `localhost:1414`        |

## API Paths

| Resource          | Gateway API        |
| ----------------- | ------------------ |
| Employees         | `/api/employees`   |
| Missions          | `/api/missions`    |
| Messages          | `/api/messages`    |
| Personal Messages | `/api/messages/my` |
| Partners          | `/api/partners`    |

> **Frontend requests should use the Gateway (`8082`) rather than calling individual backend service ports directly.**

For example:

```text
Correct:
http://localhost:8082/api/partners

Not intended for frontend:
http://localhost:8087/api/partners
```

---

# 📚 API Documentation

The backend APIs use **Swagger / OpenAPI**.

Example:

```text
http://localhost:8083/swagger-ui/index.html
```

Other microservices expose Swagger according to their individual configuration.

The central Gateway is:

```text
http://localhost:8082
```

---

# ⚛️ Frontend

The frontend is maintained in a separate repository:

```text
bankapp-frontend
```

Repository:

https://github.com/youssefJmaiel/bankapp-frontend

Technology stack:

* React
* TypeScript
* Vite
* Tailwind CSS
* Keycloak JavaScript adapter
* JWT
* REST API integration

The frontend communicates with the backend through:

```text
VITE_API_URL
      │
      ▼
API Gateway :8082
```

Example local configuration:

```env
VITE_API_URL=http://localhost:8082
VITE_KEYCLOAK_URL=http://localhost:8081
VITE_KEYCLOAK_REALM=spring-app
VITE_KEYCLOAK_CLIENT_ID=bankapp-frontend
```

---

# 🔐 Frontend Authentication

The frontend uses Keycloak for authentication.

```text
User
 │
 ▼
Keycloak
 │
 ▼
JWT
 │
 ▼
React
 │
 │ Authorization: Bearer JWT
 ▼
Gateway :8082
```

The frontend automatically includes the JWT in protected API requests.

---

# 👥 Frontend Role-Based UI

The frontend adapts the interface according to the authenticated user's role.

```text
ADMIN
 │
 ├── Management actions
 ├── Create
 ├── Update
 └── Delete

USER
 │
 ├── Consultation
 └── Personal inbox
```

These frontend restrictions are only for usability.

The backend remains responsible for security enforcement.

---

# 📸 Application Screenshots

The repository contains application screenshots under:

```text
docs/screenshots/
```

## 🔐 Keycloak Login

![Keycloak Login](docs/screenshots/01-keycloak-login.png)

---

## 🏦 Dashboard

![Dashboard](docs/screenshots/02-dashboard.png)

---

## 👥 Employees


![Employees](docs/screenshots/03-employees.png)

---

## ➕ Add Employee

![Add Employee](docs/screenshots/04-add-employee.png)

---

## 🏢 Departments

![Departments](docs/screenshots/05-departments.png)

---

## ➕ Add Department

![Add Department](docs/screenshots/06-add-department.png)

---

## 📨 Messages

![Messages](docs/screenshots/07-messages.png)

---

## 📨 Send Banking Message

![Send Banking Message](docs/screenshots/08-send-banking-message.png)

---

## 🤝 Partners

![Partners](docs/screenshots/09-partners.png)

---

## ➕ Add Partner

![Add Partner](docs/screenshots/10-add-partner.png)

---

## 📭 Partners Empty State

![Partners Empty State](docs/screenshots/11-partners-empty-state.png)

---

## 🎯 Missions

![Mission Details](docs/screenshots/misssion-details.jpeg)


![Missions](docs/screenshots/12-missions.png)

---

## ➕ Add Mission

![Add Mission](docs/screenshots/13-add-mission.png)

---

# 🧪 Testing & Validation

The project has been validated across several technical areas.

## Security

```text
JWT authentication              ✅
Keycloak integration            ✅
ADMIN authorization             ✅
USER authorization              ✅
Protected REST APIs             ✅
Gateway authentication          ✅
Personal message authorization  ✅
```

Expected HTTP behavior includes:

```text
Missing / invalid authentication
            │
            ▼
       401 Unauthorized
```

and insufficient permissions:

```text
Authenticated USER
        │
        ▼
ADMIN-only endpoint
        │
        ▼
   403 Forbidden
```

---

# 🧪 Maven Tests

Run the tests with:

```bash
mvn test
```

or, if Maven Wrapper is available:

```bash
./mvnw test
```

---

# 🔨 Backend Build

To build the backend without executing tests:

```bash
mvn clean package -DskipTests
```

---

# 📁 Repository Structure

The repository root is organized as follows:

```text
bankapp-microservice/
│
├── auth-service/
│
├── bankapp-domain/
│
├── bankapp-notification/       # Email notification microservice
├── bankapp-platform/
│
├── career-service/
│
├── common/
│
├── config-server/
│
├── discovery-server/
│
├── gateway-service/
│
├── hr-service/
│
├── message-router/
│
├── mission-service/
│
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
│       ├── 11-partners-empty-state.png
│       ├── 12-missions.png
│       └── 13-add-mission.png
│
├── docker-compose.yml
├── pom.xml
├── .env.example
├── .gitignore
└── README.md
```

---

# 🧱 Architecture Modules

The repository contains several supporting modules in addition to the business microservices.

### `auth-service`

Authentication-related backend component and integration.

### `bankapp-domain`

Shared domain components used by the application architecture.

### `bankapp-platform`

Shared platform-level components and infrastructure.

### `bankapp-notification`

Dedicated notification microservice responsible for email delivery within the BankApp ecosystem.

**Main responsibilities:**
- Exposes a REST endpoint at `POST /api/notifications/send` to request email delivery.
- Sends HTML emails using Spring Boot, `JavaMailSender`, and Thymeleaf templates.
- Renders email templates with dynamic variables, such as employee first name, last name, and email address.
- Accepts notification details including recipient, subject, template name, locale, and template variables.
- Supports configurable SMTP settings through environment variables.
- Supports local email testing with Mailpit through the configured SMTP service.
- Registers with Eureka for service discovery.

**Technologies:** Java 17, Spring Boot, Spring Mail, Thymeleaf, REST API, Eureka, SMTP, and Mailpit for local testing.

**Important distinction:** Employee account creation and activation are handled by the HR service and Keycloak. This notification microservice provides a separate email-sending API; automatic invocation by the HR service should not be assumed unless that integration is explicitly implemented.

### `career-service`

Dedicated application service module related to career/business functionality.

### `common`

Shared/common application components.

### `config-server`

Centralized configuration infrastructure.

### `discovery-server`

Eureka service discovery server.

### `gateway-service`

Central API Gateway.

### `hr-service`

Employee and department management.

### `message-router`

Banking messaging and IBM MQ integration.

### `mission-service`

Mission management and employee assignment.

### `partner-service`

Independent partner management microservice.

---

# 🛡️ Security Architecture

The application uses multiple layers of protection:

```text
                   ┌──────────────┐
                   │   Keycloak   │
                   └──────┬───────┘
                          │
                         JWT
                          │
                          ▼
                   ┌──────────────┐
                   │ API Gateway  │
                   └──────┬───────┘
                          │
                          ▼
                ┌─────────────────────┐
                │ Spring Security     │
                │ Resource Server     │
                └──────────┬──────────┘
                           │
                           ▼
                 Method Authorization
                           │
                           ▼
                    Business API
```

The architecture combines:

* centralized identity;
* token-based authentication;
* JWT validation;
* gateway security;
* microservice-level security;
* role-based authorization;
* method-level authorization.

---

# 🌍 CORS Architecture

CORS is handled centrally at the **API Gateway** level for frontend-to-backend communication.

The frontend communicates with:

```text
React
  │
  ▼
Gateway :8082
```

rather than contacting every microservice directly.

This avoids duplicate CORS headers and keeps cross-origin configuration centralized.

The Partner Service, for example, does not need its own frontend CORS configuration because browser requests are intended to enter through the Gateway.

---

# 🧠 Architectural Principles

The project follows several important microservice principles.

## Service Independence

Each business capability has its own service.

```text
HR
Mission
Message
Partner
```

## Database Ownership

Each business service owns its own PostgreSQL schema.

```text
HR       → hr_schema
Mission  → mission_schema
Message  → message_schema
Partner  → partner_schema
```

## Centralized API Entry Point

The frontend communicates through the Gateway.

```text
Frontend
    │
    ▼
Gateway
    │
    ▼
Microservices
```

## Dynamic Discovery

Service locations are discovered through Eureka rather than hard-coded IP addresses.

## Defense in Depth

Frontend visibility is combined with backend authorization.

## Controlled Database Evolution

Liquibase manages database changes while Hibernate validates the schema.

## Synchronous + Asynchronous Communication

The project combines:

```text
REST
 +
IBM MQ
```

---

# 📊 Technology Stack

| Layer                   | Technology                  |
| ----------------------- | --------------------------- |
| Language                | Java 17                     |
| Backend Framework       | Spring Boot 2.7.17          |
| Cloud Framework         | Spring Cloud 2021.0.8       |
| API Gateway             | Spring Cloud Gateway        |
| Service Discovery       | Netflix Eureka              |
| Authentication          | Keycloak                    |
| Security                | Spring Security             |
| Protocol                | OAuth2 / JWT                |
| ORM                     | Spring Data JPA / Hibernate |
| Database                | PostgreSQL                  |
| Database Migration      | Liquibase                   |
| Messaging               | IBM MQ                      |
| Build                   | Maven                       |
| Containers              | Docker                      |
| Orchestration           | Docker Compose              |
| API Documentation       | Swagger / OpenAPI           |
| Frontend                | React                       |
| Frontend Language       | TypeScript                  |
| Frontend Build          | Vite                        |
| UI                      | Tailwind CSS                |
| Frontend Authentication | Keycloak                    |
| Frontend Deployment     | Cloudflare Pages            |

---

# ☁️ Deployment

The frontend is deployed through **Cloudflare Pages**.

```text
React + TypeScript
        │
        ▼
Cloudflare Pages
        │
        ▼
API Gateway
        │
        ▼
Spring Boot Microservices
```

### Live Frontend

https://cddd48c5.bankapp-frontend-606.pages.dev

The live environment depends on the availability and configuration of the backend API and Keycloak authentication infrastructure.

---

# 🎯 Project Objectives

The main objective of **BankApp Microservices** is to demonstrate the design and implementation of an enterprise-oriented distributed application using technologies commonly used in professional Java backend environments.

The project focuses on:

* Microservices architecture
* Spring Boot
* Spring Cloud
* API Gateway
* Eureka service discovery
* OAuth2 / JWT
* Keycloak
* Spring Security
* Role-Based Access Control
* REST APIs
* PostgreSQL
* Liquibase
* IBM MQ
* Docker
* React
* TypeScript
* Frontend/backend integration
* API documentation
* Automated testing

---

# 🏆 Key Engineering Achievements

## Backend

* Designed a distributed Spring Boot microservices architecture
* Implemented API Gateway routing
* Implemented Eureka service discovery
* Integrated Keycloak
* Implemented OAuth2/JWT authentication
* Implemented role-based authorization
* Implemented method-level security
* Integrated IBM MQ
* Implemented synchronous REST communication
* Implemented asynchronous messaging
* Migrated business persistence to PostgreSQL
* Implemented Liquibase migrations
* Introduced dedicated PostgreSQL schemas
* Configured Hibernate schema validation
* Containerized backend services with Docker
* Added Swagger/OpenAPI documentation
* Added automated tests

## Partner Architecture

* Extracted partner management into a dedicated `partner-service`
* Added independent Partner persistence
* Added dedicated `partner_schema`
* Added Partner Liquibase migrations
* Registered Partner Service with Eureka
* Added Gateway routing through `PARTNER-SERVICE`
* Secured Partner APIs with Keycloak/JWT
* Implemented ADMIN/USER authorization
* Integrated Partner Service with the React frontend through the Gateway
* Centralized CORS at the Gateway level

## Frontend

* Built a React + TypeScript banking management interface
* Integrated Keycloak authentication
* Integrated JWT-based API authentication
* Implemented ADMIN/USER role-based UI
* Added employee management
* Added department management
* Added mission management
* Added partner management
* Added banking messaging
* Added personal USER message inbox
* Integrated frontend APIs through the Gateway
* Deployed frontend using Cloudflare Pages

---

# 💼 What This Project Demonstrates

From a software engineering perspective, the project demonstrates practical implementation of:

```text
Java 17
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
   ├── PostgreSQL
   ├── Liquibase
   ├── IBM MQ
   ├── Docker
   ├── Maven
   └── Swagger / OpenAPI
```

The frontend complements the architecture with:

```text
React
   │
   ├── TypeScript
   ├── Vite
   ├── Tailwind CSS
   ├── Keycloak
   ├── JWT
   ├── REST API Integration
   └── Cloudflare Pages
```

---

# 🔗 Project Links

### Backend

https://github.com/youssefJmaiel/bankapp-microservice

### Frontend

https://github.com/youssefJmaiel/bankapp-frontend

### Live Application

https://cddd48c5.bankapp-frontend-606.pages.dev

### GitHub Profile

https://github.com/youssefJmaiel

### LinkedIn

https://linkedin.com/in/youssef-jmaiel

---

# 👨‍💻 Author

## Youssef Jmaiel

**Computer Science Engineer**

Focus:

**Java Backend · Spring Boot · Microservices · Full-Stack Development**

---

# 📜 License

This project is licensed under the **MIT License**.

---

# ⭐ Final Note

**BankApp Microservices** demonstrates a complete enterprise-oriented application architecture combining:

```text
                   ┌─────────────────────┐
                   │   React Frontend    │
                   │    TypeScript       │
                   └──────────┬──────────┘
                              │
                              ▼
                   ┌─────────────────────┐
                   │   Spring Gateway    │
                   │       :8082         │
                   └──────────┬──────────┘
                              │
                     ┌────────┴────────┐
                     │     Eureka      │
                     │      :8761      │
                     └────────┬────────┘
                              │
          ┌───────────┬───────┼────────┬───────────┐
          ▼           ▼       ▼        ▼           │
       HR :8083   Mission   Message   Partner      │
                  :8084     :8085     :8087        │
                     │         │        │           │
                     ▼         ▼        ▼           │
                 PostgreSQL PostgreSQL PostgreSQL   │
                               │                    │
                               ▼                    │
                            IBM MQ                  │
                              :1414                 │
                                                   │
                         Keycloak :8081 ◀──────────┘
```

This architecture demonstrates secure, discoverable, containerized and independently organized backend services integrated with a modern web frontend.


## Employee Archive and Restore

![Archived Employees](docs/screenshots/employees-archieved.jpeg)

BankApp supports logical archiving and restoration of employee records. Instead of permanently deleting an employee from the database, the application marks the record as archived, preserving its history.

### Features

- **Archive employees:** Administrators can archive an employee from the Employees page.
- **View archived employees:** A dedicated Archived view displays archived records.
- **Restore employees:** Administrators can restore an archived employee to the active employees list.
- **Preserve employee history:** Archived records remain in PostgreSQL; the `archived` flag is separate from the employee's employment status.
- **Database migration:** Liquibase migration `005-add-employee-archive.yaml` adds the non-nullable `archived` column, defaulting to `false` for existing records.

### Backend API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/employees` | List active employees |
| `GET` | `/api/employees/archived` | List archived employees |
| `DELETE` | `/api/employees/{id}` | Logically archive an employee |
| `POST` | `/api/employees/{id}/restore` | Restore an archived employee |

The archive and restore endpoints are protected by the application's existing authorization rules.

### Frontend

The Employees page provides separate active and archived views. Administrators can archive active employees and restore archived records. The existing employee form and other pages remain separate from this feature.

### Validation

- Frontend production build completed successfully with Vite.
- Backend Maven compilation and packaging completed successfully.
- Archive, archived-list retrieval, and restore API operations were tested successfully.


---

## 📧 Mission Assignment Email Notifications

![Mission Assignment Email Notification](docs/screenshots/notification-email.jpeg)


BankApp supports automatic email notifications when a mission is assigned to an employee.

### Mission form fields

The mission form supports the following information:

- **Title** — mission title.
- **Description** — details about the mission.
- **Assigned Employee** — employee assigned to the mission.
- **Location** — mission location.
- **Start Date** — scheduled start date.
- **End Date** — scheduled end date.
- **Status** — mission status (`PENDING` by default).
- **Priority** — mission priority (`MEDIUM` by default).
- **Budget** — mission budget.

### Automatic notification workflow

1. An authorized user creates a mission and assigns it to an employee.
2. The Mission Service saves the mission and publishes a mission-assignment event.
3. The event is sent asynchronously to the IBM MQ queue `MISSION.ASSIGNED`.
4. The Notification Service consumes the event through `MissionAssignedListener`.
5. Thymeleaf renders the HTML email using the mission and employee information.
6. `JavaMailSender` sends the email to the assigned employee through the configured SMTP provider.

**Services involved:**

- `mission-service` — mission creation, assignment, and event publishing.
- `MissionEventPublisher` — publishes mission-assignment events to IBM MQ.
- `IBM MQ` — transports events asynchronously through `MISSION.ASSIGNED`.
- `bankapp-notification` — consumes events and handles email rendering and delivery.
- `MissionAssignedListener` — receives and processes mission-assignment events.

### Email template

The mission notification template is:

`bankapp-notification/src/main/resources/templates/mission-assigned.html`

It supports mission information such as the title, description, start date and end date, as well as the employee's first name. Dates are formatted as `dd/MM/yyyy`.

### Database migration

The mission details are persisted through Liquibase. The migration is:

`mission-service/src/main/resources/db/changelog/changes/002-add-mission-details.yaml`

It adds the `location`, `status`, `priority` and `budget` columns to the mission database schema.

### SMTP configuration and security

The Notification Service uses Spring `JavaMailSender` and SMTP configuration supplied through environment variables.

For Gmail, configure `smtp.gmail.com` on port `587` with authentication and STARTTLS enabled. Store the Gmail App Password in your local `.env` file.

**Security:** Never commit `.env` files, SMTP credentials, app passwords or other secrets to GitHub.

### Validation

The mission notification workflow was tested successfully: a mission-assignment event was published to IBM MQ, consumed by the Notification Service, and an email was received by the assigned employee.


---

# 💳 Payment Microservice — Account Transfers

![BankApp Payment Microservice](docs/screenshots/Bankapp-payment.jpeg)

## Overview

BankApp now includes a dedicated **Payment Microservice**, built with Java 17 and Spring Boot, to handle payment requests and coordinate transfers between bank accounts.

The service is integrated into the existing microservices architecture through Spring Cloud Gateway and Eureka Service Discovery. It runs on port `8088` and exposes its REST API through the `/api/payments/**` Gateway route.

## Key Features

- **Payment creation:** Accepts payment requests containing the source account, destination account, amount, and currency.
- **Request validation:** Validates payment data, including positive amounts, decimal precision, and currency format.
- **Unique payment references:** Generates a unique reference for each payment when needed.
- **Payment tracking:** Stores payment records and tracks their status throughout processing.
- **Account Service integration:** Delegates the actual account transfer to the Account Service instead of directly modifying account balances.
- **Secure REST API:** Uses Keycloak-issued JWT access tokens to authenticate requests and associate payments with their owners.
- **Payment history:** Allows authenticated users to retrieve their own payments.
- **Database migrations:** Uses PostgreSQL and Liquibase to manage the payment database schema.

## How It Works

1. The user submits a payment request from the React frontend.
2. The request is sent through the Spring Cloud Gateway to the Payment Microservice.
3. The service validates the request, generates a payment reference, and records the payment with an initial `PENDING` status.
4. The Payment Microservice calls the Account Service to request the actual account transfer, forwarding the authentication token.
5. The Account Service validates the accounts and transfer conditions before executing the balance movement.
6. When the transfer succeeds, the payment status becomes `COMPLETED`. If the downstream request fails, the payment is marked `FAILED` and the API reports an error.

The Payment Service and Account Service have separate responsibilities: the Payment Service manages payment requests and their status, while the Account Service owns account balances and transfer execution. Since these operations span two independent services, a network failure can leave the final transfer outcome uncertain and does not guarantee an automatic rollback across both services.

## REST API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/payments` | Creates a payment and requests the corresponding account transfer. |
| `GET` | `/api/payments/my` | Retrieves the authenticated user's payment history. |
| `GET` | `/api/payments/{id}` | Retrieves a payment belonging to the authenticated user. |

## Technology Stack

- **Language:** Java 17
- **Framework:** Spring Boot
- **Security:** Spring Security, OAuth2 Resource Server, JWT, and Keycloak
- **Architecture:** Microservices, Spring Cloud Gateway, and Eureka
- **Database:** PostgreSQL with a dedicated `payment_schema`
- **Database migrations:** Liquibase
- **Frontend integration:** React and TypeScript

This addition extends BankApp with a dedicated payment workflow while preserving the separation of responsibilities between microservices.

---

# 📊 Payment Transaction History — Frontend

![BankApp Payment Transaction History](docs/screenshots/payment-history-statistic.jpeg)

## Overview

The BankApp frontend provides a dedicated Transaction History section that allows authenticated users to review their recorded payments. This feature complements the Payment Microservice by presenting payment records in a clear, structured interface.

## Transaction History Features

### 1. Transaction Overview

The transaction history organizes payment information into a table. Depending on the data returned by the API, displayed information can include the transaction reference, source account, destination account, amount, currency, payment status, and transaction date.

### 2. Authenticated Payment History

The frontend retrieves the authenticated user's payment history through the following REST endpoint:

`GET /api/payments/my`

The request is routed through the API Gateway to the Payment Microservice. Authentication relies on the existing Keycloak and JWT security configuration. The backend is responsible for associating payment records with their owners and enforcing access controls.

### 3. Payment Status

Payment status labels help users identify the recorded outcome of a transaction. The displayed status reflects the information returned by the backend.

### 4. Refreshing Payment History

The transaction history can be refreshed to retrieve the latest available payment records. After a payment submission, the frontend can reload the history to display updated information when supported by its implementation.

### 5. Loading, Empty, and Error States

The interface should account for loading, empty, error, and populated states, helping users understand whether records are being retrieved, no transactions are available, or a request has failed.

## Frontend and Backend Integration

The feature connects the React and TypeScript frontend to the existing BankApp microservices architecture.

The Payment Microservice manages payment records and their statuses, while the Account Service remains responsible for account balances and transfer execution. This separation of responsibilities supports a modular and maintainable architecture.

## Technologies

- **React and TypeScript** — frontend development.
- **Tailwind CSS** — interface styling.
- **REST API** — communication with backend services.
- **Spring Cloud Gateway** — API routing.
- **Spring Boot** — payment service implementation.
- **Keycloak and JWT** — authentication and secure API access.
- **PostgreSQL and Liquibase** — data persistence and database migrations.

## Summary

The Payment Transaction History feature provides authenticated users with a dedicated interface for reviewing recorded transfers. It integrates the frontend with the Payment Microservice through a secured REST API while preserving the separation between payment record management and account transfer execution.


---

# Leave Management Microservice

## Overview

The **Leave Management Microservice** is a new feature of the BankApp Microservices project. It provides a dedicated workflow for employees to submit and track leave requests and for administrators to review, approve, or reject them.

This service separates leave management from the other business domains in BankApp. It is implemented with Spring Boot and integrated with Spring Cloud Gateway, Eureka service discovery, Keycloak/JWT authentication, PostgreSQL, and Liquibase database migrations.

## 1. User Interface — Employee Leave Requests

![Leave Management - User Interface](docs/screenshots/leave-interface-user.jpeg)

The User interface allows authenticated employees to manage their own leave requests.

### Main capabilities

- **Submit leave requests:** Employees can create a leave request through the frontend form.
- **View personal requests:** Employees can retrieve and review their own requests.
- **Track request status:** Employees can follow the status returned by the service, such as pending, approved, or rejected.
- **Request cancellation:** Employees can request cancellation of their own leave requests, subject to backend business rules.
- **Personalized access:** The backend identifies the current user from the authenticated JWT token.

This interface gives employees a dedicated place to submit requests and follow their progress without requiring access to administrative functions.


## 2. Admin Interface — Leave Request Management

![Leave Management - Admin Interface](docs/screenshots/leave-interface-admin.jpeg)

The Admin interface provides a centralized area for managing employee leave requests.

### Main capabilities

- **View all requests:** Administrators can retrieve the leave requests available for review.
- **Review employee requests:** Administrators can inspect the information returned by the service before making a decision.
- **Approve requests:** Administrators can approve a leave request through the administrative decision endpoint.
- **Reject requests:** Administrators can reject a request using the same endpoint with a different decision value.
- **Protected administration:** The backend restricts the endpoint for listing all requests and the decision endpoint to users with the `ADMIN` role.

Administrative permissions are enforced by the backend, not only by the visibility of frontend controls.

## 3. Leave Request Workflow

1. An authenticated employee submits a leave request through the frontend.
2. The frontend sends the request through the BankApp API Gateway.
3. The Gateway routes the request to the Leave Management Microservice.
4. The service processes the request and associates it with the authenticated user.
5. The employee retrieves their personal requests to follow their progress.
6. An administrator retrieves and reviews the requests.
7. The administrator approves or rejects a request.
8. The updated request can be retrieved by the relevant interface.

The backend remains responsible for authorization, request processing, and persistence.


## 4. REST API Endpoints

The Leave Management Microservice exposes the following REST endpoints through the BankApp API Gateway.

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/api/leaves` | USER or ADMIN | Create a leave request. |
| GET | `/api/leaves/my` | USER or ADMIN | Retrieve the authenticated user's requests. |
| DELETE | `/api/leaves/{id}` | USER or ADMIN | Request cancellation of the user's own request. |
| GET | `/api/leaves/all` | ADMIN | Retrieve all leave requests. |
| PATCH | `/api/leaves/{id}/decision` | ADMIN | Approve or reject a leave request. |


### Example: Approve a leave request

An administrator can approve a leave request by sending a PATCH request to `/api/leaves/{id}/decision` with a valid access token and the ADMIN role.

Example JSON request body:

    {"decision": "APPROVED"}

To reject a request, use `REJECTED` as the decision value. The backend checks the administrator permissions before processing the decision.


## 5. Microservice Architecture and Integration

The Leave Management Microservice integrates with the existing BankApp infrastructure.

    React + TypeScript Frontend
               |
               v
    Spring Cloud Gateway (Port 8082)
               |
               v
         Eureka Discovery
               |
               v
    Leave Service (Port 8090)
               |
               v
    PostgreSQL - leave_schema

### Components

- **React and TypeScript:** Provide the User and Admin interfaces.
- **Spring Boot:** Implements leave request management and administrative decisions.
- **Spring Cloud Gateway:** Routes `/api/leaves/**` requests to the Leave Service.
- **Eureka:** Supports discovery of `LEAVE-SERVICE`.
- **Keycloak and JWT:** Provide authentication and role-based access control.
- **PostgreSQL:** Stores leave data in the dedicated `leave_schema` schema.
- **Liquibase:** Manages database schema migrations.
- **Docker Compose:** Supports the local multi-service environment.

The dedicated Leave Service keeps leave-related business logic separate from other BankApp services and integrates through the existing API Gateway and service discovery infrastructure.
