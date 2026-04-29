# Development Roadmap

## Current Foundation

The project now has a Spring Boot backend starter with domain entities, repositories, service interfaces, service implementations, controller skeletons, DTOs, validation, error handling, JWT authentication, role-based access control, secured evidence workflow, authenticated discovery search, basic LevelPlay scoring, admin moderation/audit logging, and tests.

## Phase 1: Harden MVP Foundation

- Add database migrations with Flyway or Liquibase.
- Completed: replace permissive security with JWT authentication.
- Completed: enforce route-level role-based access control.
- Completed: add registration, login, and current-user endpoints.
- Completed: add integration tests for authentication and protected endpoints.
- Add refresh token flow.
- Add OpenAPI/Swagger documentation.
- Add Docker Compose for PostgreSQL.
- Add CI workflow for `mvn test`.

## Phase 2: Complete Core Workflows

- Completed: implement evidence upload/link submission and verification workflow using secured role gates.
- Completed: implement athlete search and discovery using VERIFIED evidence and basic profile filters.
- Add profile update endpoints.
- Add organisation roster relationships.
- Add coach/institution approval workflows.
- Add verification queues for coaches and administrators.
- Completed: add admin moderation endpoints for flagged/archived evidence and moderation notes.
- Completed: add audit logging for evidence flag/archive and admin LevelPlay recalculation actions.
- Add full admin moderation queue workflow for assignment, review outcome, and appeals.
- Completed: add discovery search pagination and sorting.

## Phase 3: Evidence Storage

- Add managed file upload flow.
- Store files in local dev storage and object storage in production.
- Add file type, size, and malware scanning controls.
- Add signed URL access.
- Add upload retry/resume support.
- Add metadata validation for sport-specific evidence.

## Phase 4: LevelPlay Refinement

- Completed: implement basic LevelPlayScore calculation using verified evidence count, achievement count, coach verification count, and profile completeness score.
- Completed: define transparent MVP scoring rules and expose score explanations.
- Version scoring formulas.
- Add score history.
- Separate leaderboards by sport, age group, region, and position/event.
- Add fairness review reports.
- Expand audit logging around score formula changes and score history once those features exist.

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
