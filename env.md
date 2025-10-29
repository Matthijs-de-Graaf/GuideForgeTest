# GuideForge Project Overview

Full-stack app using **Spring Boot (Java 21)**, **Vite React**, **MySQL**, **Docker**, and **GitHub Actions CI/CD**.

---

## 1. Workflow

| Environment | Purpose                          | Command / Setup                                       |
| ----------- | -------------------------------- | ----------------------------------------------------- |
| Development | Hot reload for local development | `docker compose -f docker-compose.dev.yml up --build` |
| Production  | Deployed version                 | `docker compose up -d` (uses images from Docker Hub)  |
| CI/CD       | Automated testing & deployment   | GitHub Actions workflows (`.github/workflows/`)       |

**Security:** Use `.env` or GitHub Secrets for passwords and API URLs; never hardcode sensitive data.

---

## 2. Key Project Files

### Backend (`./backend/`)

* **pom.xml** – Maven dependencies & plugins for Spring Boot, MySQL, Lombok, Testcontainers, and OWASP dependency checks.
* **src/main/resources/application.properties** – Production/dev DB configuration & JPA settings.
* **src/test/resources/application.properties** – Test DB configuration.
* **Dockerfile** – Multi-stage build for production: Maven → JAR → Java runtime.
* **Dockerfile.dev** – Development image with Maven for hot reload.
* **ContainerTestConfig.java** – Testcontainers config for running integration tests with MySQL in CI.

### Frontend (`./frontend/`)

* **Dockerfile** – Multi-stage build for production: Node → Build → Nginx.
* **Dockerfile.dev** – Dev image with Vite for hot reload.
* **nginx.conf** – Nginx config for serving SPA with caching rules.

### Docker

* **docker-entrypoint-initdb.d/init.sql** – SQL script executed on MySQL container start.
* **docker-compose.yml** – Production setup: MySQL + backend + frontend.
* **docker-compose.dev.yml** – Development setup: bind mounts + hot reload.
* **docker-compose.test.yml** – Test setup for integration CI/CD.

### CI/CD (`.github/workflows/`)

| Workflow             | Purpose                                                              |
| -------------------- | -------------------------------------------------------------------- |
| `backend-ci.yml`     | Run backend unit tests, dependency scans on feature/develop branches |
| `frontend-ci.yml`    | Run frontend lint, tests, build, and npm audit                       |
| `integration-ci.yml` | End-to-end integration tests in containerized environment            |
| `docker-deploy.yml`  | Build/push Docker images and deploy to server (on master branch)     |

---

## 3. Git & Branch Workflow

* **feature/*:** Fast CI (unit tests, lint).
* **develop:** Full CI + integration tests.
* **master:** Build Docker images + deploy to production.

**Reasoning:** Prevent broken code from reaching production; branch isolation ensures safe development.

---

## 4. GitHub Secrets (Environment Variables)

| Secret                                                   | Purpose                     |
| -------------------------------------------------------- | --------------------------- |
| `MYSQL_ROOT_PASSWORD`                                    | MySQL root password         |
| `MYSQL_DATABASE`                                         | Name of database            |
| `MYSQL_USER`                                             | Database user for app       |
| `MYSQL_PASSWORD`                                         | User password               |
| `DOCKER_USERNAME`                                        | Docker Hub username         |
| `DOCKER_PASSWORD`                                        | Docker Hub token/password   |
| `REACT_APP_API_URL`                                      | Frontend API endpoint       |
| Optional: `SERVER_HOST`, `SERVER_USER`, `SERVER_SSH_KEY` | Auto-deploy SSH credentials |

**Rule:** Secrets are not stored in code, only in `.env` or GitHub.

---

## 5. Backend CI (Example: `backend-ci.yml`)

* Runs on push/PR for `feature/*` and `develop`.
* Starts MySQL container with health checks.
* Sets up **JDK 21**, caches Maven dependencies.
* Runs **unit tests** and **OWASP Dependency Check**.
* Uploads reports as artifacts.

```yaml
# Simplified example
jobs:
  build-test:
    runs-on: ubuntu-latest
    services:
      db:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: ${{ secrets.MYSQL_ROOT_PASSWORD }}
          MYSQL_DATABASE: ${{ secrets.MYSQL_DATABASE }}
          MYSQL_USER: ${{ secrets.MYSQL_USER }}
          MYSQL_PASSWORD: ${{ secrets.MYSQL_PASSWORD }}
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: 'temurin', cache: maven }
      - run: ./mvnw clean test
```

---

## 6. Frontend CI (`frontend-ci.yml`)

* Sets up **Node 20**, caches dependencies.
* Runs lint, tests, build, and `npm audit`.
* Ensures production-ready frontend.

---

## 7. Integration CI (`integration-ci.yml`)

* Runs full stack: MySQL, backend, frontend.
* Uses Docker Compose (`docker-compose.test.yml`) for production-like environment.
* Optional **OWASP ZAP scan** for runtime vulnerabilities.

---

## 8. Docker Deploy (`docker-deploy.yml`)

* Triggered on `master`.
* Builds backend & frontend Docker images.
* Pushes images to Docker Hub (`:latest` + `:sha` tags).
* Optional: SSH deploy to server.

---

## 9. Dockerfiles

### Backend Production

```dockerfile
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./ 
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseZGC", "-jar", "app.jar"]
```

### Backend Development

* Uses Maven + hot reload (`spring-boot:run`), bind mounts for code changes.

### Frontend Production

```dockerfile
FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Frontend Development

* Uses Node + Vite dev server, bind mounts, hot reload, port 5173 exposed.

---

## 10. Docker Compose

### Production (`docker-compose.yml`)

* Services: `db`, `backend`, `frontend`.
* Uses Docker Hub images.
* Secrets via `.env`.
* Networks: bridge, volumes for DB persistence.

### Development (`docker-compose.dev.yml`)

* Services with bind mounts.
* Hot reload enabled.
* Local ports: backend 8080, frontend 5173.

### Test (`docker-compose.test.yml`)

* Production-like environment for CI integration tests.
* Healthchecks ensure services are ready before tests.

---

## 11. Best Practices

* Separate dev/prod environments.
* Cache dependencies for speed.
* Scan dependencies for vulnerabilities.
* Test locally before pushing.
* Use branch protection in GitHub.

---

## 12. Local Development & Debugging

```bash
# Development
docker compose -f docker-compose.dev.yml up --build

# Production
docker compose up -d

# View logs
docker compose logs -f
```

---

## 13. Deployment Overview

* Setup server: install Docker & Compose.
* Pull Docker Hub images: `docker compose pull`.
* Start services: `docker compose up -d`.
* Use CI/CD for automated build and deploy.
* Ensure `.env` with secrets is present on server.
