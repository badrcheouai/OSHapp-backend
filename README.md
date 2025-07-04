# OSHapp – Occupational Safety & Health backend

Spring Boot REST API that powers **OSHapp**, an occupational-health & safety (OHS) platform.  
It exposes secure endpoints for workforce medical files, risk assessments, scheduling, and reporting, protected with Keycloak / OAuth 2.0.

---

## 1. Tech stack

| Layer            | Tech / Version | Notes |
|------------------|----------------|-------|
| Language & JDK   | **Java 17**    | AdoptOpenJDK / Temurin 17+ |
| Framework        | **Spring Boot 3.5.3** | Web, Data JPA, Validation, Security, OAuth2 (client & resource-server) :contentReference[oaicite:0]{index=0}|
| Build tool       | Maven (Wrapper committed) |
| Database         | PostgreSQL 16 (Docker) :contentReference[oaicite:1]{index=1}|
| AuthN/AuthZ      | Keycloak 25 (Docker) |
| Dev helpers      | Lombok, Spring DevTools |

---

## 2. Quick-start (local)

```bash
# ① Clone
git clone https://github.com/badrcheouai/OSHapp-backend.git
cd OSHapp-backend

# ② Spin up infrastructure (Postgres + Keycloak)
docker compose -f infra/docker-compose.yml up -d

# ③ Build & run the API
./mvnw spring-boot:run   # hot-reload via DevTools
