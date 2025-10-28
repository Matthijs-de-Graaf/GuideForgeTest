## Hoe het Hele Project Werkt

- **Workflow**:
  - **Development**: Run lokaal met `docker-compose.dev.yml` voor hot reloading (veranderingen in code worden direct zichtbaar zonder restarts).
  - **Productie**: Run met `docker-compose.yml` op een server, met images van Docker Hub.
  - **Security**: Secrets (wachtwoorden, API URLs) in `.env` of GitHub Secrets, geen hardcoded waarden.

## Gemaakte Files en Wat Ze Doen

Hier is een lijst die ik heb toegevoegd per categorie. Elke file heeft een korte uitleg.

### Backend Files (in `./backend/`)

- **pom.xml**: Definieert afhankelijkheden (bijv. Spring Boot starters, MySQL driver, Lombok) en plugins (bijv. compiler voor Lombok). Dit bestand beheert de build en dependencies voor Maven.
- **src/main/resources/application.properties**: Configuratie voor de backend in productie/dev (bijv. database connectie, JPA settings). Zorgt dat Spring Boot weet hoe met MySQL te verbinden.
- **src/test/resources/application.properties**: Configuratie voor tests (bijv. test database connectie). Zorgt dat tests een aparte omgeving gebruiken zonder productie data te beïnvloeden.
- **Dockerfile**: Multi-stage Dockerfile voor backend productie (Maven voor build, Java JRE voor runtime).
- **Dockerfile.dev**: Dockerfile voor backend development (Maven voor hot reload).

### Frontend Files (in `./frontend/`)

- **Dockerfile**: Multi-stage Dockerfile voor productie (Node voor build, Nginx voor serving).
- **Dockerfile.dev**: Dockerfile voor development (Node voor Vite dev server).
- **nginx.conf**: Nginx configuratie voor productie (SPA routing, caching).

### Docker Files (in project root)

- **docker-entrypoint-initdb.d/init.sql**: SQL script voor MySQL initialisatie (stelt `root` wachtwoord in).
- **docker-entrypoint-initdb.d**: Map voor MySQL initialisatie scripts (wordt uitgevoerd bij container start).

### Docker Compose Files (in project root)

- **docker-compose.yml**: Voor productie; start MySQL, backend, frontend met Docker Hub images.
- **docker-compose.dev.yml**: Voor development; start MySQL, backend, frontend met bind mounts voor hot reload.
- **docker-compose.test.yml**: Voor tests; start MySQL, backend, frontend met productie Dockerfiles.

### CI/CD Workflows (in `.github/workflows/`)

- **backend-ci.yml**: Test backend unit tests en dependencies op feature/develop branches.
- **frontend-ci.yml**: Test frontend linting, tests, build op feature/develop branches.
- **integration-ci.yml**: Test de volledige stack (end-to-end) op develop branch met Docker Compose en ZAP scan.
- **docker-deploy.yml**: Bouwt en pusht images naar Docker Hub op main branch, optioneel deployt naar server.

### Andere Files (in project root)

- **.env**: Environment variabelen voor secrets (bijv. MySQL wachtwoorden, API URLs). Gebruikt door Docker Compose.
- **README.md**: Overzicht van het project, setup, en usage.
- **improvements.md**: Dit document, met samenvatting van de setup en debugging.

### Hoe de Files Samenwerken

- **Backend**: `pom.xml` bouwt de app met Maven. `application.properties` configureert de database. `Book.java` defineert de data structuur. `BookRepository.java` handelt database interacties. `BookController.java` biedt API endpoints. `BookJsonDataLoader.java` laadt initiële data. `BackendApplicationTests.java` test de context.
- **Frontend**: Vite bouwt de React app, Nginx serveert in productie.
- **Docker**: Dockerfiles bouwen containers. Docker Compose start de stack (DB, backend, frontend) samen.
- **CI/CD**: Workflows testen code bij pushes/PRs en deployen images bij merges naar main.

## Hoe de CI/CD Pipelines Werken

De pipelines gebruiken GitHub Actions om code te testen, te bouwen, en te deployen.

- **Triggers**:
  - `feature/*`: Snelle CI tests (build, unit tests).
  - `develop`: CI + integration tests (end-to-end met Docker Compose).
  - `main`: Volledige CI/CD + image build/push naar Docker Hub, optioneel deploy naar server.

- **Backend CI (backend-ci.yml)**:
  - Start MySQL service met secrets.
  - Setup Java 21, cache dependencies.
  - Run unit tests met Maven.
  - Scan dependencies op vulnerabilities met OWASP Dependency-Check.

- **Frontend CI (frontend-ci.yml)**:
  - Setup Node 20, cache dependencies.
  - Run linting, tests, build met npm.
  - Scan dependencies op vulnerabilities met npm audit.

- **Integration CI (integration-ci.yml)**:
  - Start MySQL service.
  - Setup Java 21 en Node 20, cache dependencies.
  - Bouw backend en frontend.
  - Run Docker Compose tests met `docker-compose.test.yml`.
  - Run OWASP ZAP scan voor runtime vulnerabilities.

- **Docker Deploy (docker-deploy.yml)**:
  - Login bij Docker Hub met secrets.
  - Bouw en push backend/frontend images met tags (`:latest`, `:sha`).
  - Optioneel: Deploy naar server via SSH (pull images, run Docker Compose).

- **Hoe het Werkt in GitHub**:
  - Bij push/PR: Workflows runnen automatisch.
  - Fouten blokkeren merges (via branch protection rules in GitHub Settings).
  - Secrets houden gevoelige data veilig.

- **Lokale Simulatie**:
  - Run workflows lokaal met `act` tool (installeer via `brew install act` of equivalent).

## Stap 1: Overzicht van de Pipeline Structuur

We maken vier workflows in `.github/workflows/`:

- `backend-ci.yml`: Test de backend (Spring Boot).
- `frontend-ci.yml`: Test de frontend (Vite React).
- `integration-ci.yml`: Test de hele app samen (end-to-end).
- `docker-deploy.yml`: Bouwt en pusht Docker images naar Docker Hub, en (optioneel) deployt naar een server.

**Git Workflow (hoe branches werken):**

- **feature/* branches**: Run alleen snelle CI tests (build en unit tests).
- **develop branch**: Run CI + integration tests.
- **master branch**: Run volledige CI/CD + deploy naar productie.

**Waarom?** Dit voorkomt dat kapotte code in productie komt. Voor beginners: Een "branch" is een kopie van je code om veilig te experimenteren.

**Actie:** Maak de map `.github/workflows/` in je repository en kopieer de YAML-bestanden daarheen (zie latere stappen).

## Stap 2: GitHub Secrets Instellen

Secrets zijn verborgen variabelen voor gevoelige data, zoals wachtwoorden. Geen hardcoded waarden in code!

**Stappen om in te stellen (voor beginners):**

1. Ga naar je GitHub repository.
2. Klik op "Settings" > "Secrets and variables" > "Actions".
3. Klik "New repository secret" voor elk:
   - `MYSQL_ROOT_PASSWORD`: Root wachtwoord voor MySQL.
   - `MYSQL_DATABASE`: Naam van je database (bijv. `guideforge_db`).
   - `MYSQL_USER`: Gebruiker voor app (bijv. `app_user`).
   - `MYSQL_PASSWORD`: Wachtwoord voor app gebruiker.
   - `DOCKER_USERNAME`: Je Docker Hub username (bijv. `matthijs`).
   - `DOCKER_PASSWORD`: Je Docker Hub wachtwoord of access token.
   - `REACT_APP_API_URL`: API URL voor frontend (bijv. `http://backend:8080/api`).
   - Optioneel: `SERVER_HOST`, `SERVER_USER`, `SERVER_SSH_KEY` voor auto-deploy.

**Waarom?** Veiligheid: Secrets lekken niet in code of logs.

## Stap 3: Backend CI Workflow (backend-ci.yml)

Deze workflow test de backend op feature en develop branches.

**Voor beginners:** Dit runt automatisch bij elke push/PR. Het start een tijdelijke MySQL database voor tests.

**Kopieerbare code voor `.github/workflows/backend-ci.yml`:**

```yaml
name: Backend CI

on:
  push:
    branches: [ "develop", "feature/*" ]
  pull_request:
    branches: [ "develop", "master" ]
  workflow_dispatch:

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
        options: >-
          --health-cmd="mysqladmin ping --silent"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven  # Cache Maven dependencies

      - name: Run unit tests
        working-directory: ./backend
        run: ./mvnw clean test  # Alleen unit tests
        env:
          SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/${{ secrets.MYSQL_DATABASE }}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Amsterdam
          SPRING_DATASOURCE_USERNAME: ${{ secrets.MYSQL_USER }}
          SPRING_DATASOURCE_PASSWORD: ${{ secrets.MYSQL_PASSWORD }}

      - name: OWASP Dependency Check
        uses: dependency-check/Dependency-Check_Action@main
        with:
          project: 'backend'
          path: './backend'
          format: 'HTML,JSON'
          failOnCVSS: '7'

      - name: Upload Dependency-Check Report
        uses: actions/upload-artifact@v4
        with:
          name: dep-check-report
          path: dependency-check-report.html
```

**Stap-voor-stap uitleg:**

1. **on:** Wanneer het runt (bij push/PR).
2. **services:** Start MySQL container met secrets. Healthcheck wacht tot DB ready.
3. **steps:** Check out code, setup Java 21 (met caching), run tests met Maven, scan dependencies op vulnerabilities, en upload rapport.
4. **Waarom caching?** Bespaart tijd door dependencies niet telkens te downloaden.
5. **Run het:** Commit en push; bekijk in GitHub "Actions" tab.

## Stap 4: Frontend CI Workflow (frontend-ci.yml)

Deze test de frontend.

**Voor beginners:** Runt npm commando's om te checken of code schoon is.

**Kopieerbare code voor `.github/workflows/frontend-ci.yml`:**

```yaml
name: Frontend CI

on:
  push:
    branches: [ "develop", "feature/*" ]
  pull_request:
    branches: [ "develop", "master" ]
  workflow_dispatch:

jobs:
  build-test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: 20
          cache: 'npm'  # Cache npm dependencies
          cache-dependency-path: ./frontend/package-lock.json

      - name: Install dependencies and test
        working-directory: ./frontend
        run: |
          npm ci
          npm run lint
          npm test -- --coverage  # Voeg coverage toe
          npm run build  # Bouw voor consistentie
        env:
          REACT_APP_API_URL: ${{ secrets.REACT_APP_API_URL }}

      - name: NPM Audit
        working-directory: ./frontend
        run: npm audit --audit-level=critical || exit 1  # Fail op kritieke vulns
```

**Stap-voor-stap uitleg:**

1. **on:** Zelfde als backend.
2. **steps:** Setup Node 20 (met caching), installeer dependencies, run lint/tests/build, en audit dependencies.
3. **Waarom coverage?** Toont hoeveel code getest is.
4. **Waarom build?** Zorgt dat productie build werkt.

## Stap 5: Integration CI Workflow (integration-ci.yml)

Deze test de hele app samen op develop.

**Voor beginners:** Dit simuleert je app in containers voor echte tests.

**Kopieerbare code voor `.github/workflows/integration-ci.yml`:**

```yaml
name: Integration CI

on:
  push:
    branches: [ "develop" ]
  pull_request:
    branches: [ "develop" ]
  workflow_dispatch:

jobs:
  integration-test:
    runs-on: ubuntu-latest
    services:
      db:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: ${{ secrets.MYSQL_ROOT_PASSWORD }}
          MYSQL_DATABASE: ${{ secrets.MYSQL_DATABASE }}
          MYSQL_USER: ${{ secrets.MYSQL_USER }}
          MYSQL_PASSWORD: ${{ secrets.MYSQL_PASSWORD }}
        options: >-
          --health-cmd="mysqladmin ping --silent"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: 20
          cache: 'npm'
          cache-dependency-path: ./frontend/package-lock.json

      - name: Build backend
        working-directory: ./backend
        run: ./mvnw clean package
        env:
          SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/${{ secrets.MYSQL_DATABASE }}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Amsterdam
          SPRING_DATASOURCE_USERNAME: ${{ secrets.MYSQL_USER }}
          SPRING_DATASOURCE_PASSWORD: ${{ secrets.MYSQL_PASSWORD }}

      - name: Build frontend
        working-directory: ./frontend
        run: npm run build
        env:
          REACT_APP_API_URL: http://backend:8080/api

      - name: Docker Compose Integration
        run: docker-compose -f docker-compose.test.yml up --build --abort-on-container-exit

      - name: Run OWASP ZAP Scan
        uses: zaproxy/action-full-scan@v0.10.0
        with:
          target: 'http://backend:8080'
          token: ${{ secrets.GITHUB_TOKEN }}
          cmd_options: '-a'
```

**Stap-voor-stap uitleg:**

1. **on:** Alleen op develop.
2. **services:** MySQL zoals in backend-ci.
3. **steps:** Setup, bouw backend/frontend, run Docker Compose voor tests, en scan met ZAP op vulnerabilities.
4. **ZAP Scan:** Test runtime security (bijv. SQL injection).
5. **Docker Compose:** Gebruikt `docker-compose.test.yml` (zie stap 6).

## Stap 6: Docker Deploy Workflow (docker-deploy.yml)

Deze bouwt en pusht images naar Docker Hub op master.

**Kopieerbare code voor `.github/workflows/docker-deploy.yml`:**

```yaml
name: Docker Build & Deploy

on:
  push:
    branches: [ "master" ]
  workflow_dispatch:

jobs:
  docker:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}

      - name: Build and push backend image
        uses: docker/build-push-action@v5
        with:
          context: ./backend
          file: ./backend/Dockerfile
          push: true
          tags: ${{ secrets.DOCKER_USERNAME }}/guideforge-backend:latest,${{ secrets.DOCKER_USERNAME }}/guideforge-backend:${{ github.sha }}

      - name: Build and push frontend image
        uses: docker/build-push-action@v5
        with:
          context: ./frontend
          file: ./frontend/Dockerfile
          push: true
          tags: ${{ secrets.DOCKER_USERNAME }}/guideforge-frontend:latest,${{ secrets.DOCKER_USERNAME }}/guideforge-frontend:${{ github.sha }}

      # Optioneel: Deploy naar server via SSH
      - name: Deploy to Server
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          script: |
            cd /path/to/repo
            git pull
            docker compose pull
            docker compose up -d
```

**Stap-voor-stap uitleg:**

1. **on:** Op master of handmatig.
2. **steps:** Login bij Docker Hub, bouw en push images met tags (latest + SHA voor versioning).
3. **SSH (optioneel):** Automatisch deployen op server.
4. **Waarom tags?** `:latest` voor makkelijk, `:sha` voor rollbacks.

## Stap 7: Dockerfiles voor Backend en Frontend

Deze bouwen je app in containers.

### Backend Dockerfile (./backend/Dockerfile) - Productie

**Kopieerbare code:**

```dockerfile
# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseZGC", "-jar", "app.jar"]
```

**Uitleg voor beginners:**

1. **Build stage:** Compileert code met Maven naar JAR.
2. **Runtime stage:** Draait JAR in lichte Java image.
3. **Test lokaal:** `docker build -t guideforge-backend .` en `docker run -p 8080:8080 guideforge-backend`.

### Frontend Dockerfile (./frontend/Dockerfile) - Productie

**Kopieerbare code:**

```dockerfile
# Build stage
FROM node:20 AS build
WORKDIR /app
COPY package*.json .
RUN npm ci
COPY . .
RUN npm run build

# Runtime stage
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**nginx.conf (./frontend/nginx.conf):**

```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location ~* \.(?:css|js|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        access_log off;
        add_header Cache-Control "public";
    }
}
```

**Uitleg:** Bouwt React naar statische files, serveert met Nginx.

### Development Dockerfiles

Voor dev: `./backend/Dockerfile.dev` en `./frontend/Dockerfile.dev` (zie eerdere chat voor code; gebruik Maven/Vite voor hot reload).

## Stap 8: docker-compose.yml voor Productie

**Kopieerbare code:**

```yaml
version: '3.8'
services:
  db:
    image: mysql:8.0
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - db_data:/var/lib/mysql
      - ./docker-entrypoint-initdb.d:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "--silent"]
      interval: 10s
      timeout: 5s
      retries: 3
    networks:
      - app-network

  backend:
    image: ${DOCKER_USERNAME}/guideforge-backend:latest
    restart: always
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Amsterdam
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
    ports:
      - "8080:8080"
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
    networks:
      - app-network

  frontend:
    image: ${DOCKER_USERNAME}/guideforge-frontend:latest
    restart: always
    depends_on:
      - backend
    environment:
      REACT_APP_API_URL: http://backend:8080/api
    ports:
      - "80:80"
    deploy:
      resources:
        limits:
          cpus: '0.25'
          memory: 256M
    networks:
      - app-network

networks:
  app-network:
    driver: bridge

volumes:
  db_data:
```

**Uitleg:** Start DB, backend, frontend. Gebruik `.env` op server voor secrets. Run: `docker compose up -d`.

## Stap 9: docker-compose.dev.yml voor Development

**Kopieerbare code:** (Zie eerdere chat; met bind mounts voor hot reload).

**Uitleg:** Voor lokale dev; run `docker compose -f docker-compose.dev.yml up --build`.

## Stap 10: docker-compose.test.yml voor Tests

**Kopieerbare code:**

```yaml
version: '3.8'
services:
  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - ./docker-entrypoint-initdb.d:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "--silent"]
      interval: 10s
      timeout: 5s
      retries: 3
    networks:
      - app-network

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Amsterdam
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
    networks:
      - app-network

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    depends_on:
      - backend
    environment:
      REACT_APP_API_URL: http://backend:8080/api
    networks:
      - app-network

networks:
  app-network:
    driver: bridge
```

**Uitleg:** Voor integration-ci; test productie-like setup.

## Stap 11: Deployment op Server

1. Setup server (bijv. DigitalOcean Droplet met Ubuntu).
2. Installeer Docker/Compose: `sudo apt update && sudo apt install docker.io docker-compose`.
3. Clone repo, maak `.env`, run `docker compose pull && up -d`.
4. Voor auto-deploy: Voeg SSH secrets toe en gebruik de workflow.

**Uitleg over Docker Hub:** Docker Hub is een online repository voor images. We push images daarheen zodat je server ze kan pullen. Nodig voor distributie en consistentie.

## Stap 12: README.md Update

**Kopieerbare code:**

```markdown
# GuideForge Project

Full-stack app met Spring Boot (Java 21), Vite React, MySQL, Docker.

## Setup
1. Clone repo.
2. Setup secrets in GitHub.
3. Voor dev: `docker compose -f docker-compose.dev.yml up`.
4. Voor prod: Zie deployment.

## CI/CD
Workflows in `.github/workflows/`.

## Licentie
MIT.
```

**Uitleg:** Update dit in je repo root.

## Stap 13: Best Practices en Afsluiting

- Scheid dev/prod.
- Gebruik caching voor snelheid.
- Scan altijd op security.
- Test lokaal voordat pushen.


## Volgende Stappen en Tips

- **Lokaal Run**:
  - Development: `docker compose -f docker-compose.dev.yml up --build`.
  - Productie: `docker compose up`.
- **Debugging**: Bekijk logs met `docker compose logs` of GitHub Actions logs.
- **Security**: Gebruik sterke wachtwoorden in productie, niet `root`.
- **Uitbreidingen**: Voeg meer endpoints toe aan `BookController` of frontend integratie.

Dit document is compleet en kan als referentie gebruikt worden. Als je wijzigingen wilt, laat het weten!