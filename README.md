# 🚀 Task Management Service

![CI](https://github.com/chinmay-vijapure-05/TaskManagerService/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green)
![Build](https://img.shields.io/badge/Build-Maven-orange)
![Docker](https://img.shields.io/badge/Containerized-Docker-blue)
![Tests](https://img.shields.io/badge/Tests-JUnit5-brightgreen)

A backend system built using modern **Spring Boot 3** practices to demonstrate secure REST API development, clean architecture, real-time communication, testing strategies, and containerized deployment.

> ⚡ Built as a portfolio project to showcase backend engineering skills and real-world system design practices.

---

## 🎯 Purpose

This project was built to demonstrate:

- Secure REST API development using Spring Boot
- Clean layered architecture and separation of concerns
- JWT-based authentication & role-based authorization
- Integration testing using Testcontainers
- Real-time updates using WebSockets
- Containerized deployment using Docker
- Production-style exception handling and validation

This is not a tutorial-style implementation — it reflects real backend engineering practices.

---

## 🧩 Features

### 🔐 Authentication & Security
- JWT-based authentication
- Role-based access control (USER / ADMIN)
- Spring Security integration
- Stateless authentication
- Secure password hashing
- Global exception handling
- Input validation with Hibernate Validator

### 📁 Project Management
- Create, update, delete projects
- User-specific project ownership
- Secure project-level access control

### ✅ Task Management
- Create, update, delete tasks
- Task status tracking (TODO, IN_PROGRESS, DONE)
- Priority levels (LOW, MEDIUM, HIGH, URGENT)
- Task search with filters (status, priority)
- Pagination support

### 🔔 Real-Time Updates
- WebSocket-based project notifications
- Project-level broadcast messaging
- User-specific notification queue

### ⚡ Performance & Optimization
- Redis caching
- Clean layered architecture
- DTO-based API structure
- Separation of persistence and presentation layers

### 🧪 Testing
- Unit tests (Service & Utility layers)
- Integration tests (Controller layer)
- Testcontainers (PostgreSQL)
- JWT validation tests
- Validation & exception handling tests

### 🐳 DevOps
- Dockerized services
- PostgreSQL container
- Redis container
- Health checks via Actuator

---

## 🏗️ Architecture

```
Client (Postman / Frontend)
        │
        ▼
Spring Boot Application
        │
        ├── Controller Layer (REST APIs)
        ├── Service Layer (Business Logic)
        ├── Repository Layer (JPA)
        ├── Security Layer (JWT Filters)
        ├── WebSocket Layer
        └── Global Exception Handling
        │
        ▼
PostgreSQL (Primary Database)
Redis (Caching Layer)
```

### Architectural Principles

- Clean layered architecture
- Separation of concerns
- DTO-based communication
- Centralized exception handling
- Stateless authentication (JWT)
- Real-time event-driven updates

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Security | Spring Security + JWT |
| Database | PostgreSQL |
| Cache | Redis |
| ORM | Spring Data JPA / Hibernate |
| Real-Time | WebSocket (STOMP) |
| Testing | JUnit 5, Mockito, Testcontainers |
| Build Tool | Maven |
| DevOps | Docker |
| Monitoring | Spring Boot Actuator |

---

## ⚙️ Local Setup

### 1️⃣ Clone Repository

```bash
git clone https://github.com/chinmay-vijapure-05/TaskManagerService.git
cd TaskManagerService
```

### 2️⃣ Configure Environment Variables

Create `application.properties`:

```properties
jwt.secret=secret-key
jwt.expiration=86400000

spring.datasource.url=jdbc:postgresql://localhost:5432/taskdb
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### 3️⃣ Run Application

```bash
mvn clean install
mvn spring-boot:run
```

Application runs at:

```
http://localhost:8080
```

---

## 🐳 Run with Docker

```bash
docker-compose up --build
```

Services:

- App → `localhost:8080`
- PostgreSQL → `localhost:5432`
- Redis → `localhost:6379`

---

## 🧪 Testing Strategy

The project includes:

- ✅ Unit tests for service and utility layers
- ✅ Integration tests for REST endpoints
- ✅ Testcontainers for isolated PostgreSQL testing
- ✅ JWT validation tests
- ✅ Validation and exception handling tests

Run tests:

```bash
mvn clean test
```

---

## 📡 API Endpoints

### Authentication
- `POST /api/auth/register`
- `POST /api/auth/login`

### Projects
- `POST /api/projects`
- `GET /api/projects`
- `GET /api/projects/{id}`
- `PUT /api/projects/{id}`
- `DELETE /api/projects/{id}`

### Tasks
- `POST /api/tasks`
- `GET /api/tasks/project/{projectId}`
- `GET /api/tasks/search`
- `PUT /api/tasks/{id}`
- `DELETE /api/tasks/{id}`

---

## 🧪 Sample API Request

### Register User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email":"user@test.com",
    "password":"password123",
    "fullName":"Test User"
  }'
```

### Sample Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@test.com",
  "fullName": "Test User"
}
```

---

## 📊 Monitoring

Available endpoints:

- `/actuator/health`
- `/actuator/metrics`
- `/actuator/info`

---

## 🔐 Security Highlights

- Stateless JWT authentication
- Role-based endpoint protection
- Secure password hashing
- Custom JWT utility
- Centralized exception management

---

## 🚀 Future Improvements

- CI/CD pipeline (GitHub Actions)
- Swagger / OpenAPI documentation
- Rate limiting
- Email notifications
- File attachments for tasks
- Kubernetes deployment
- Microservice decomposition

---

## 👨‍💻 Author

**Chinmay Vijapure**  
Java Backend Developer

---

## 📄 License

This project is built for educational and portfolio purposes.
