# Task Manager - Spring Boot

A real-time collaborative task management platform built with Spring Boot.

## Features (In Progress)
- ✅ JWT-based authentication
- ✅ User registration and login
- 🚧 Task and project management (coming soon)
- 🚧 Real-time collaboration with WebSockets
- 🚧 Event-driven architecture with Kafka

## Tech Stack
- Spring Boot 3.x
- PostgreSQL
- Spring Security + JWT
- Docker

## Setup

### Prerequisites
- Java 17+
- Docker (for PostgreSQL)

### Running Locally

1. Start PostgreSQL:
```bash
docker run --name taskmanager-db -e POSTGRES_PASSWORD=password -e POSTGRES_DB=taskmanager -p 5432:5432 -d postgres:15
```

2. Run application:
```bash
./mvnw spring-boot:run
```

3. Test endpoints:
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password123","fullName":"Test User"}'
```

## Project Status
🚧 **Work in Progress** - Building this as a portfolio project to showcase Spring Boot expertise.

---
**Author**: Your Name  
**Contact**: your.email@example.com