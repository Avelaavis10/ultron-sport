# Ultron Sport

Ultron Sport is a mobile-first sports talent evidence platform for making grassroots talent visible, verified, and discoverable.

The platform helps athletes upload sporting evidence, coaches and institutions verify that evidence, and scouts or agents discover talent using search, filters, and a fair credibility system called LevelPlay Rank.

## Core MVP

- Athlete registration and profile setup
- Evidence upload for videos, statistics, certificates, and achievements
- Coach and institution verification workflows
- Talent discovery through search, filters, shortlists, and profile views
- LevelPlay Rank for credibility and fair comparison
- Notifications for verification results, ranking updates, and recruiter activity
- Admin tools for moderation, roles, compliance, and audit trails

## Primary Users

- Athletes who need a credible place to showcase performance
- Coaches and institutions who validate athlete evidence
- Scouts and agents who search for verified talent
- Fans and community members who follow athlete journeys
- Administrators who manage trust, safety, and compliance

## Documentation

- [Product Overview](docs/product-overview.md)
- [Software Requirements Specification](docs/software-requirements-specification.md)
- [Architecture Specification](docs/architecture-specification.md)
- [Improvement Roadmap](docs/improvement-roadmap.md)
- [LevelPlay Rank](docs/levelplay-rank.md)
- [Data Model](docs/data-model.md)
- [Security and Privacy](docs/security-and-privacy.md)
- [UI and Prototype Notes](docs/ui-and-prototype-notes.md)

## Source Materials

This documentation is based on the Ultron Sport SRS, improvement report, overview and architecture specification, Drive assets, and Figma MVP mockups.

The prototype covers mobile app flows for onboarding, profile setup, home feed, player profile, video upload, scout and organization dashboards, location search, fan community, plus desktop-style web, streaming, and analytics views.

## Technical Direction

The recommended production direction is a mobile app backed by a web portal and modular backend services. The architecture should support secure media upload, verification, search, ranking, notifications, analytics, and future AI analysis.

The current MVP documentation prioritizes credibility, ranking fairness, privacy, and practical phased delivery over premature complexity.

## Backend MVP Stack

- Language: Java 17+
- Framework: Spring Boot 3
- API style: REST with JSON
- Persistence: Spring Data JPA
- Security: Spring Security, BCrypt password hashing, JWT bearer tokens
- Default local database: H2 in memory
- Production database target: PostgreSQL
- Build tool: Maven

## Run the Backend

Prerequisites:

- JDK 17 or newer
- Maven 3.9 or newer

Start the API locally with the default H2 database:

```powershell
mvn spring-boot:run
```

Optional local JWT overrides:

```powershell
$env:ULTRON_JWT_SECRET="replace-with-a-long-local-development-secret"
$env:ULTRON_JWT_EXPIRATION_MINUTES="60"
```

Run the automated tests:

```powershell
mvn test
```

Run with PostgreSQL:

```powershell
$env:SPRING_PROFILES_ACTIVE="postgres"
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/ultron_sport"
$env:DATABASE_USERNAME="ultron"
$env:DATABASE_PASSWORD="ultron"
mvn spring-boot:run
```

The initial API base path is:

```text
http://localhost:8080/api/v1
```

Authentication endpoints are exposed at:

```text
http://localhost:8080/api/auth
```

Evidence workflow endpoints are exposed at:

```text
http://localhost:8080/api/evidence
```

Discovery endpoints are exposed at:

```text
http://localhost:8080/api/discovery
```

Use `POST /api/auth/register` or `POST /api/auth/login` to receive a bearer token, then call protected endpoints with:

```text
Authorization: Bearer <accessToken>
```

For the MVP, evidence uses `fileUrl` or `externalVideoLink` placeholders. Direct file upload, object storage, CDN delivery, malware scanning, and AI analysis jobs are intentionally deferred.

Discovery search is database-backed with pagination and role-aware evidence visibility. Scouts and organisations see verified evidence only; admins can filter all evidence statuses.

The MVP backend is intentionally a modular monolith. It separates domain, repositories, services, controllers, DTOs, security configuration, and error handling so the codebase can later evolve toward microservices.
