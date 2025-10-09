# Project Samenvatting: GuideForge Full-Stack Applicatie met CI/CD Pipeline

Dit document geeft een overzicht van alles wat we in deze discussie hebben opgezet voor je GuideForge project. We hebben een full-stack applicatie gebouwd met een Spring Boot backend, Vite React frontend, MySQL database, Docker voor containerization, en GitHub Actions voor CI/CD. Ik focus op de gemaakte files, wat ze doen, hoe het hele project werkt, en hoe de CI/CD pipelines functioneren. Alles is opgezet om professioneel te zijn, met scheiding tussen development en productie.

## Hoe het Hele Project Werkt

- **Overzicht**: GuideForge is een full-stack applicatie voor het beheren van boeken. De backend (Spring Boot) biedt een REST API voor CRUD-operaties op boeken (create, read, update, delete). De frontend (Vite React) communiceert met de backend via API calls. MySQL slaat de data op (bijv. boeken met auteurs en uitgevers). Docker containerizeert de app voor consistente execution, en GitHub Actions automatiseert testen, builds, en deployments.
- **Workflow**:
  - **Development**: Run lokaal met `docker-compose.dev.yml` voor hot reloading (veranderingen in code worden direct zichtbaar zonder restarts).
  - **Productie**: Run met `docker-compose.yml` op een server, met images van Docker Hub.
  - **Data Flow**: Frontend roept backend API's aan (bijv. `/api/books/all` voor alle boeken). Backend gebruikt JPA om data op te slaan in MySQL. Bij opstarten laadt `BookJsonDataLoader` data uit `books.json` als de DB leeg is.
  - **Security**: Secrets (wachtwoorden, API URLs) in `.env` of GitHub Secrets, geen hardcoded waarden.
- **Key Features**:
  - Backend: REST endpoints voor boeken, data loading uit JSON, JPA voor database interactie.
  - Frontend: Vite voor snelle development, Nginx voor productie serving.
  - Database: MySQL met initialisatie scripts.
  - CI/CD: Automatische testen en deployments via GitHub Actions.

## Gemaakte Files en Wat Ze Doen

Hier is een lijst van de belangrijkste files die we hebben aangemaakt of bijgewerkt, gegroepeerd per categorie. Elke file wordt kort uitgelegd, inclusief wat hij doet en hoe hij in het project past.

### Backend Files (in `./backend/`)

- **pom.xml**: Definieert afhankelijkheden (bijv. Spring Boot starters, MySQL driver, Lombok) en plugins (bijv. compiler voor Lombok). Dit bestand beheert de build en dependencies voor Maven.
- **src/main/resources/application.properties**: Configuratie voor de backend in productie/dev (bijv. database connectie, JPA settings). Zorgt dat Spring Boot weet hoe met MySQL te verbinden.
- **src/test/resources/application.properties**: Configuratie voor tests (bijv. test database connectie). Zorgt dat tests een aparte omgeving gebruiken zonder productie data te beïnvloeden.
- **src/main/java/com/guideforge/backend/book/Author.java**: Record class voor auteur data (naam, geboortedatum, land). Gebruikt als ingebedde object in `Book`.
- **src/main/java/com/guideforge/backend/book/Publisher.java**: Record class voor uitgever data (naam, stad, land). Gebruikt als ingebedde object in `Book`.
- **src/main/java/com/guideforge/backend/book/Language.java**: Enum voor taal (bijv. ENGLISH, DUTCH). Wordt gebruikt in `Book` om talen te representeren.
- **src/main/java/com/guideforge/backend/book/Book.java**: Record class voor boek data (id, titel, ISBN, etc.). De hoofd entiteit die in de database wordt opgeslagen.
- **src/main/java/com/guideforge/backend/book/BookRepository.java**: JPA repository interface voor CRUD-operaties op boeken. Handelt database interacties af (findAll, save, delete, count).
- **src/main/java/com/guideforge/backend/book/BookController.java**: REST controller voor API endpoints (GET, POST, PUT, DELETE voor boeken). Beheert HTTP verzoeken en responsen.
- **src/main/java/com/guideforge/backend/book/BookJsonDataLoader.java**: CommandLineRunner die boeken laadt uit `/data/books.json` bij opstarten als de DB leeg is. Vult de database met initiële data.
- **src/main/java/com/guideforge/backend/book/BookNotFoundException.java**: Custom exception voor 404 fouten als een boek niet bestaat. Wordt gebruikt in de controller.
- **src/test/java/com/guideforge/backend/BackendApplicationTests.java**: Basis testclass om te controleren of de Spring context correct laadt. Gebruikt `@SpringBootTest`.

### Frontend Files (in `./frontend/`)

- **Dockerfile**: Multi-stage Dockerfile voor productie (Node voor build, Nginx voor serving).
- **Dockerfile.dev**: Dockerfile voor development (Node voor Vite dev server).
- **nginx.conf**: Nginx configuratie voor productie (SPA routing, caching).

### Docker Files (in project root)

- **Dockerfile (backend)**: Multi-stage Dockerfile voor backend productie (Maven voor build, Java JRE voor runtime).
- **Dockerfile.dev (backend)**: Dockerfile voor backend development (Maven voor hot reload).
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

## Volgende Stappen en Tips

- **Lokaal Run**:
  - Development: `docker compose -f docker-compose.dev.yml up --build`.
  - Productie: `docker compose up`.
- **Debugging**: Bekijk logs met `docker compose logs` of GitHub Actions logs.
- **Security**: Gebruik sterke wachtwoorden in productie, niet `root`.
- **Uitbreidingen**: Voeg meer endpoints toe aan `BookController` of frontend integratie.

Dit document is compleet en kan als referentie gebruikt worden. Als je wijzigingen wilt, laat het weten!
