# Architecture Overview

## Technology Choices

- Java 17+
- Spring Boot 3
- Spring Web for REST APIs
- Spring Data JPA for persistence
- Spring Security with BCrypt password hashing, JWT bearer tokens, and RBAC
- PostgreSQL target database, with H2 for local development and tests
- Maven for builds and tests

## MVP Architecture Style

Ultron Sport starts as a modular monolith. This keeps the MVP simple to develop and test while preserving boundaries that can later become microservices.

## Package Structure

```text
za.co.ultronsport
  common/error        Shared API error handling
  config/security    JWT config, token service, request filter, user details service, RBAC rules
  config/storage     Media storage mode, local path, public URL, and size/type settings
  config             Application metadata configuration
  domain             Entities, enums, and domain state transitions
  repository         Spring Data repository interfaces
  service            Service interfaces
  service/impl       Service implementations and domain orchestration
  web/controller     REST controllers
  web/dto            Request and response DTOs
```

## Current MVP Modules

- Authentication with registration, login, JWT validation, and current-user lookup
- User and role foundation
- Athlete profile management with current-user ownership, `/me` update flow, and completeness recalculation
- Coach profile verification support with organisation context
- Organisation, school, club, academy, university, and team records
- Evidence upload/link submission with structured metadata, draft/submission lifecycle, and AI-ready status
- Local/mock media upload abstraction with metadata, checksum storage, and evidence attachment
- Evidence verification workflow for coach approval/rejection and admin flag/archive moderation
- Athlete search and discovery using verified evidence and basic profile filters
- Achievement creation/update with athlete ownership and LevelPlay recalculation
- LevelPlay credibility score calculation with transparent MVP score explanation
- Admin moderation and append-only audit logging foundation
- In-app notification model for key evidence, moderation, LevelPlay, profile, achievement, organisation, and coach profile events
- MVP operational hardening with public health/readiness/version endpoints and consistent error JSON
- Global validation and API error handling
- Service unit tests and JPA repository integration test
- MockMvc security integration tests for JWT and role-protected endpoints
- Discovery service tests and MockMvc tests for role-aware search visibility
- LevelPlay service and MockMvc tests for scoring, recalculation, explanation, and endpoint access control
- Admin moderation/audit service and MockMvc tests for audit visibility, flagged/archived evidence, notes, and role access
- Media storage service and MockMvc tests for upload, metadata visibility, and evidence attachment
- Athlete profile and achievement service/MockMvc tests for ownership, duplicate prevention, updates, and LevelPlay integration
- Coach/organisation relationship tests for organisation creation, coach profile ownership, athlete organisation linking, verification context, and protected access
- Notification service and MockMvc tests for current-user notification access, read status, workflow notification creation, and protected access
- Health and error-handling integration tests for operational readiness and developer handover

## Security Flow

1. Public clients register or log in through `/api/auth/register` and `/api/auth/login`.
2. Passwords are stored with BCrypt hashes only.
3. Successful authentication returns a short-lived JWT bearer token.
4. `JwtAuthenticationFilter` validates bearer tokens before protected requests reach controllers.
5. `SecurityConfig` applies role rules for ATHLETE, COACH, ORGANISATION, SCOUT_AGENT, and ADMIN endpoints.
6. Authentication and authorisation failures are written as the standard API error JSON shape.

JWT settings are read from `security.jwt.*` configuration. Local defaults exist for development, but deployed environments must override `ULTRON_JWT_SECRET`.

## Design Rules

- Controllers map HTTP requests only; they do not own domain decisions.
- Services coordinate use cases and enforce workflow rules.
- Domain entities hold simple state transitions.
- DTOs isolate API contracts from persistence models.
- Repositories are persistence adapters.
- Security is enforced at the HTTP boundary, while services remain the place for use-case and workflow rules.
- Future security work includes refresh tokens, password reset, account lockout, rate limiting, MFA, OAuth/social login, and POPIA privacy controls.

## Operational Hardening

Public MVP health endpoints live under `/api/health`.

- `/api/health` returns application status, configured app name, environment, and timestamp.
- `/api/health/readiness` performs a small database connectivity check and returns READY when the database is reachable.
- `/api/health/version` returns configured app name, version, environment, and timestamp.

The API uses one error response structure for validation, authentication, authorisation, missing resources, malformed requests, unsupported media types, method errors, and fallback failures. Responses include `code` and `traceId` for developer support but do not expose stack traces, JWT internals, passwords, or filesystem paths.

This deliberately avoids Prometheus, Grafana, ELK, OpenTelemetry, distributed tracing, SIEM, alerting, Kafka, Redis, Kubernetes, Docker Compose, and external monitoring platforms.

## Evidence Workflow

Evidence is implemented as a secured MVP workflow behind `/api/evidence`.

- Athletes create DRAFT evidence against their own athlete profile.
- Athletes may update only DRAFT or REJECTED evidence.
- Submitting evidence moves it to PENDING_VERIFICATION.
- Coaches can view pending evidence, then verify or reject it.
- Admins can view all evidence, flag evidence, archive evidence, and inspect simple verification history.
- Scouts and organisations can read VERIFIED evidence only.

The current media strategy supports URL-only evidence through `fileUrl` or `externalVideoLink`, plus a local/mock `MediaStorageService` for MVP uploads. Uploaded media creates a `MediaAsset` with owner, athlete profile, content type, checksum, public URL, upload status, and scan status. Athletes can attach their own media to their own DRAFT or REJECTED evidence, which updates the evidence `fileUrl`.

Production object storage, signed URLs, CDN delivery, malware scanning, thumbnails, transcoding, chunked uploads, background processing, and AI analysis dispatch are intentionally deferred. AI readiness is represented by `AiAnalysisStatus`, which defaults to `NOT_STARTED`; no model or AI service is invoked yet.

## Coach Organisation Verification Context

Evidence verification is now tied to a clearer MVP trust context.

- Coaches must create a CoachProfile before approving or rejecting pending evidence.
- CoachProfile may link to an Organisation record and stores certification, qualification summary, sport, and years of experience.
- AthleteProfile may link to an Organisation record while keeping `schoolOrClub` as a fallback.
- VerificationRequest records the evidence, athlete profile, coach profile, coach organisation, and whether coach and athlete share the same organisation.
- Coaches from a different organisation are still allowed to verify for MVP, but the context is recorded for admin review and future policy refinement.

This deliberately avoids external school/federation APIs, OCR, legal identity verification, automated coach verification, and advanced scoring changes.

## Discovery Workflow

Discovery is implemented behind `/api/discovery` with authenticated access only.

- SCOUT_AGENT and ORGANISATION users see athletes and evidence backed by VERIFIED evidence only.
- ATHLETE users can search public/verified athlete profiles and evidence.
- COACH users can see VERIFIED evidence and PENDING_VERIFICATION evidence relevant to verification.
- ADMIN users can search all athlete profiles and filter all evidence statuses.
- Search uses Spring Data JPA Specifications, pagination, and database indexes for MVP-scale efficiency.

Future discovery work can move high-volume search into OpenSearch/Elasticsearch, add caching, and layer recommendation signals without changing the core domain model.

## LevelPlay Workflow

LevelPlay is implemented behind `/api/levelplay` as a simple, explainable credibility score.

- One athlete profile has one current LevelPlayScore record.
- Scores are created when missing and updated on recalculation.
- Coach verification of evidence automatically recalculates the linked athlete's score.
- Athlete profile creation/update, achievement creation/update, and coach evidence verification trigger recalculation.
- Admin users can recalculate one score or all scores for MVP maintenance.
- The explanation endpoint returns the stored input counts, component scores, final score, tier, and a fairness note.

The MVP formula uses verified evidence count, achievement count, coach verification count, and profile completeness. Profile completeness is calculated from linked display name, sport, position, location, organisation or school/club, bio, age, at least one achievement, and at least one evidence item. It does not use likes, views, fan votes, popularity, paid boosts, or AI scoring. Future work can add score history, formula versioning, category leaderboards, and AI-assisted analysis after fairness review.

## Notification Workflow

Notifications are implemented behind `/api/notifications` as a database-backed in-app model.

- Users can list their own notifications, list unread notifications, count unread notifications, mark one notification read, or mark all owned notifications read.
- Notifications are append-only through the service/API surface; no delete or edit endpoints are exposed.
- Evidence verification, rejection, flagging, and archiving notify the athlete owner.
- Evidence submission currently notifies admins as an MVP fallback until coach targeting is backed by roster/team relationships.
- LevelPlay recalculation notifies the athlete only when an existing score or tier changes. Recalculate-all intentionally avoids notification fan-out in the MVP.
- Profile updates, organisation links, achievement creation, and coach profile saves create simple in-app notifications.

Email, SMS, push notifications, WebSockets, Kafka/RabbitMQ/Redis, notification preferences, and AI-triggered notifications are deliberately deferred.

## Admin Moderation And Audit Workflow

Admin moderation is implemented behind `/api/admin` with ADMIN-only access.

- Evidence flag and archive actions create AdminActionLog records.
- Admin LevelPlay recalculation creates AdminActionLog records.
- Moderation notes are stored as AdminActionLog records and do not change evidence status.
- Audit logs can be searched by action type, target type, target ID, admin user ID, and date range.
- Audit logs are append-only through the service/API surface: no edit or delete methods are exposed.
- Viewing verification history is not logged in the MVP to avoid noisy audit trails; state-changing moderation actions are logged first.

The MVP deliberately avoids SIEM integration, Kafka/event streaming, Redis, external logging platforms, automated fraud detection, and AI moderation. Those can be added later behind the same audit boundaries if needed.

## Evolution Path

Future microservice candidates:

- Auth Service
- Profile Service
- Evidence Service
- Verification Service
- Ranking Service
- Search Service
- Admin/Moderation Service
- Notification Service
- AI Analysis Service

The MVP should not split these prematurely. Keep module boundaries clear first.
