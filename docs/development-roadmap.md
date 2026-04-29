# Development Roadmap

## Current Foundation

The project now has a Spring Boot backend starter with domain entities, repositories, service interfaces, service implementations, controller skeletons, DTOs, validation, error handling, security placeholders, and tests.

## Phase 1: Harden MVP Foundation

- Add database migrations with Flyway or Liquibase.
- Replace permissive security with JWT authentication.
- Enforce role-based access at controller/service level.
- Add login endpoint and token refresh flow.
- Add integration tests for key controllers.
- Add OpenAPI/Swagger documentation.
- Add Docker Compose for PostgreSQL.
- Add CI workflow for `mvn test`.

## Phase 2: Complete Core Workflows

- Add profile update endpoints.
- Add organisation roster relationships.
- Add coach/institution approval workflows.
- Add verification queues for coaches and administrators.
- Add admin moderation queue for flagged evidence.
- Add audit logging across verification and admin actions.
- Add search pagination and sorting.

## Phase 3: Evidence Storage

- Add managed file upload flow.
- Store files in local dev storage and object storage in production.
- Add file type, size, and malware scanning controls.
- Add signed URL access.
- Add upload retry/resume support.
- Add metadata validation for sport-specific evidence.

## Phase 4: LevelPlay Refinement

- Define transparent MVP scoring rules with stakeholders.
- Version scoring formulas.
- Add score history.
- Separate leaderboards by sport, age group, region, and position/event.
- Add fairness review reports.

## Phase 5: AI-Ready Expansion

- Add AI analysis job table and event queue.
- Add model version fields and confidence scores.
- Add key-moment labels for video evidence.
- Add AI status transitions and retry handling.
- Keep AI outputs advisory until validated for fairness and accuracy.

## Guardrails

- Do not add advanced AI, live streaming, sponsorships, drone footage, or complex ranking automation before the MVP foundation is stable.
- Keep APIs small and traceable to documented requirements.
- Keep domain logic out of controllers.
- Add tests with every new service or workflow.
