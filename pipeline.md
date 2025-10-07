## Local Setup

1. Clone de repo: `git clone <repo-url>`.
2. Backend: `cd backend && ./mvnw spring-boot:run` (configureer application.properties met DB creds).
3. Frontend: `cd frontend && npm install && npm run dev`.
4. DB: Run MySQL lokaal of via Docker: `docker run -p 3306:3306 -e MYSQL_ROOT_PASSWORD=yourpass mysql:8.0`.
5. Voor full stack: Gebruik `docker-compose up` (maak een docker-compose.yml).

## CI/CD Pipeline

We gebruiken GitHub Actions voor CI/CD. Workflows in `.github/workflows/`:

- **backend-ci.yml**: Bouwt en test backend op feature/* en develop. Gebruikt MySQL service.
- **frontend-ci.yml**: Bouwt en test frontend.
- **integration-ci.yml**: End-to-end tests op develop.
- **docker-deploy.yml**: Bouwt en pusht Docker images naar GHCR op main.

### Git Workflow

- Feature branches (`feature/*`): Run CI (build & unit tests).
- Develop: Run CI + integration tests.
- Main: Run CI/CD + deploy (image push).

### GitHub Secrets Instellen

Ga naar repo Settings > Secrets and variables > Actions > New repository secret:

- `MYSQL_ROOT_PASSWORD`
- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `REACT_APP_API_URL` (bijv. <https://api.example.com>)
- Geen DOCKER secrets meer nodig (gebruik GHCR met GITHUB_TOKEN).

### Manual Triggers

Gebruik workflow_dispatch om workflows handmatig te runnen via GitHub UI.

### Best Practices

- Geen secrets in code.
- Caching voor snellere builds.
- Security scans ingebouwd.
- Voor productie deploy: Voeg een server stap toe in docker-deploy.yml.

## Bijdragen

Fork, maak feature branch, PR naar develop. Zorg dat tests passeren!

## Licentie

MIT (of pas aan).
