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
- [Admin Moderation and Audit](docs/admin-moderation-and-audit.md)
- [Media Storage](docs/media-storage.md)
- [Athlete Profiles and Achievements](docs/athlete-profile-and-achievements.md)
- [Coach Organisation Verification Context](docs/coach-organisation-verification-context.md)
- [Notifications and Events](docs/notifications-and-events.md)
- [Health Checks](docs/health-checks.md)
- [Error Handling](docs/error-handling.md)
- [Local Development Guide](docs/local-development-guide.md)
- [API Testing Guide](docs/api-testing-guide.md)
- [Role Endpoint Access Matrix](docs/role-endpoint-access-matrix.md)
- [Manual Testing Seed Data](docs/manual-testing-seed-data.md)
- [Manual HTTP Request Collection](docs/http/ultron-sport-mvp.http)
- [Frontend and Mobile Integration Plan](docs/frontend-mobile-integration-plan.md)
- [MVP Screen Map](docs/mvp-screen-map.md)
- [API to Screen Mapping](docs/api-to-screen-mapping.md)
- [Frontend API Client Strategy](docs/frontend-api-client-strategy.md)
- [Role-Based Navigation Plan](docs/role-based-navigation-plan.md)
- [Form Validation Mapping](docs/form-validation-mapping.md)
- [Frontend Manual Testing Checklist](docs/frontend-manual-testing-checklist.md)
- [Frontend Prototype](docs/frontend-prototype.md)
- [React Prototype README](frontend/README.md)
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
- Media storage: configurable LOCAL or MOCK MVP adapter, object-storage-ready interface
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

Optional local media storage overrides:

```powershell
$env:ULTRON_STORAGE_MODE="LOCAL"
$env:ULTRON_STORAGE_LOCAL_BASE_PATH="./uploads/ultron-sport"
$env:ULTRON_STORAGE_PUBLIC_BASE_URL="http://localhost:8080/media"
$env:ULTRON_STORAGE_MAX_FILE_SIZE_BYTES="52428800"
$env:ULTRON_MULTIPART_MAX_FILE_SIZE="50MB"
$env:ULTRON_MULTIPART_MAX_REQUEST_SIZE="50MB"
```

Run the automated tests:

```powershell
mvn test
```

## Run the React Prototype

The repository now includes a minimal React web prototype in `frontend/` for validating the backend API from a browser. It is not the production frontend or mobile app.

Start the backend first:

```powershell
mvn spring-boot:run
```

Then start the frontend:

```powershell
Set-Location frontend
npm install
npm run dev
```

The frontend uses:

```text
VITE_API_BASE_URL=http://localhost:8080
```

The Vite dev server runs at:

```text
http://localhost:5173
```

The backend permits that local origin by default through `ULTRON_CORS_ALLOWED_ORIGINS`. The prototype stores the access token in `sessionStorage` for MVP manual testing only.

Run with PostgreSQL:

```powershell
$env:SPRING_PROFILES_ACTIVE="postgres"
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/ultron_sport"
$env:DATABASE_USERNAME="ultron"
$env:DATABASE_PASSWORD="ultron"
mvn spring-boot:run
```

The primary MVP API base path is:

```text
http://localhost:8080/api
```

Some `/api/v1/...` endpoints remain as legacy compatibility routes from earlier MVP foundation work. New clients should use the `/api/...` contract documented in `docs/api-endpoints-draft.md`.

Authentication endpoints are exposed at:

```text
http://localhost:8080/api/auth
```

Evidence workflow endpoints are exposed at:

```text
http://localhost:8080/api/evidence
```

Athlete profile and achievement endpoints are exposed at:

```text
http://localhost:8080/api/athlete-profiles
http://localhost:8080/api/achievements
```

Organisation and coach profile endpoints are exposed at:

```text
http://localhost:8080/api/organisations
http://localhost:8080/api/coach-profiles
```

Discovery endpoints are exposed at:

```text
http://localhost:8080/api/discovery
```

LevelPlay score endpoints are exposed at:

```text
http://localhost:8080/api/levelplay
```

Admin moderation and audit endpoints are exposed at:

```text
http://localhost:8080/api/admin
```

Media upload endpoints are exposed at:

```text
http://localhost:8080/api/media
```

In-app notification endpoints are exposed at:

```text
http://localhost:8080/api/notifications
```

Health endpoints are public and exposed at:

```text
http://localhost:8080/api/health
```

Use `POST /api/auth/register` or `POST /api/auth/login` to receive a bearer token, then call protected endpoints with:

```text
Authorization: Bearer <accessToken>
```

For the MVP, evidence still supports URL-only mode through `fileUrl` or `externalVideoLink`. Athletes can also upload supported media through the local/mock `MediaStorageService` and attach it to DRAFT or REJECTED evidence. Production object storage, CDN delivery, malware scanning, transcoding, thumbnails, chunked upload, and AI analysis jobs are intentionally deferred.

Discovery search is database-backed with pagination and role-aware evidence visibility. Scouts and organisations see verified evidence only; admins can filter all evidence statuses.

LevelPlay Rank currently uses a transparent MVP formula only. It scores verified evidence count, achievement count, coach verification count, and profile completeness, then maps the final credibility score to BRONZE, SILVER, GOLD, or ELITE. It does not use popularity, likes, views, fan votes, paid boosts, or AI scoring.

Profile completeness uses nine deterministic factors: linked display name, sport, position, location, organisation or school/club, bio, age, at least one achievement, and at least one evidence item. Athlete profile updates and achievement changes trigger LevelPlay recalculation.

Coach verification now requires a coach profile before evidence can be approved or rejected. Verification history records the coach profile, coach organisation, athlete profile, and whether the coach and athlete share an organisation context. Athletes can link their profile to an organisation record while retaining `schoolOrClub` text as a fallback.

Admin moderation now records append-only audit logs for evidence flag/archive actions, moderation notes, and admin LevelPlay recalculations. The MVP intentionally avoids enterprise SIEM, Kafka, Redis, external logging platforms, automated fraud detection, and AI moderation.

In-app notifications are now database-backed and append-only. Users can list notifications, see unread notifications, count unread items, and mark one or all notifications as read. Evidence decisions, moderation outcomes, profile/achievement changes, organisation links, coach profile saves, and LevelPlay score changes create notifications where appropriate. Email, SMS, push, WebSockets, external queues, and notification preferences are intentionally deferred.

MVP operational hardening now includes custom public health, readiness, and version endpoints plus a consistent API error response for validation, authentication, authorisation, not-found, malformed request, method, and media-type failures. Error responses include a `code` and `traceId` without exposing stack traces, JWT internals, passwords, or filesystem paths.

Manual testing readiness now includes a role-based endpoint matrix, seed-data guide, and `.http` request collection for the full MVP flow from registration through evidence verification, discovery, notifications, and admin moderation.

Frontend/mobile integration readiness now includes a recommended React web prototype first approach, screen map, API-to-screen mapping, API client strategy, role-based navigation plan, form validation mapping, manual QA checklist, and reference TypeScript DTOs. This is planning only; no production frontend or mobile app has been added yet.

The React web prototype scaffold now exists under `frontend/`. It consumes the documented backend API through small TypeScript API modules and role-based dashboard shells for ATHLETE, COACH, ORGANISATION, SCOUT_AGENT, and ADMIN users.

The MVP backend is intentionally a modular monolith. It separates domain, repositories, services, controllers, DTOs, security configuration, and error handling so the codebase can later evolve toward microservices.
