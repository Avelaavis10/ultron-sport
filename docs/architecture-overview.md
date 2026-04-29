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
- Athlete profile management
- Coach profile verification support
- Organisation, school, and club records
- Evidence upload/link submission with structured metadata
- Evidence verification request workflow
- Athlete search and filtering foundation
- LevelPlay credibility score placeholder
- Admin action log and moderation foundation
- Global validation and API error handling
- Service unit tests and JPA repository integration test
- MockMvc security integration tests for JWT and role-protected endpoints

## Security Flow

1. Public clients register or log in through `/api/auth/register` and `/api/auth/login`.
2. Passwords are stored with BCrypt hashes only.
3. Successful authentication returns a short-lived JWT bearer token.
4. `JwtAuthenticationFilter` validates bearer tokens before protected requests reach controllers.
5. `SecurityConfig` applies role rules for ATHLETE, COACH, ORGANISATION, SCOUT_AGENT, and ADMIN endpoints.

JWT settings are read from `security.jwt.*` configuration. Local defaults exist for development, but deployed environments must override `ULTRON_JWT_SECRET`.

## Design Rules

- Controllers map HTTP requests only; they do not own domain decisions.
- Services coordinate use cases and enforce workflow rules.
- Domain entities hold simple state transitions.
- DTOs isolate API contracts from persistence models.
- Repositories are persistence adapters.
- Security is enforced at the HTTP boundary, while services remain the place for use-case and workflow rules.
- Future security work includes refresh tokens, password reset, account lockout, rate limiting, MFA, OAuth/social login, and POPIA privacy controls.

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
