# Architecture Overview

## Technology Choices

- Java 17+
- Spring Boot 3
- Spring Web for REST APIs
- Spring Data JPA for persistence
- Spring Security foundation for future JWT and RBAC
- PostgreSQL target database, with H2 for local development and tests
- Maven for builds and tests

## MVP Architecture Style

Ultron Sport starts as a modular monolith. This keeps the MVP simple to develop and test while preserving boundaries that can later become microservices.

## Package Structure

```text
za.co.ultronsport
  common/error        Shared API error handling
  config/security    Security placeholders and password hashing bean
  domain             Entities, enums, and domain state transitions
  repository         Spring Data repository interfaces
  service            Service interfaces
  service/impl       Service implementations and domain orchestration
  web/controller     REST controllers
  web/dto            Request and response DTOs
```

## Current MVP Modules

- Authentication and role-ready user registration
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

## Design Rules

- Controllers map HTTP requests only; they do not own domain decisions.
- Services coordinate use cases and enforce workflow rules.
- Domain entities hold simple state transitions.
- DTOs isolate API contracts from persistence models.
- Repositories are persistence adapters.
- Security is permissive for the starter, with TODOs for JWT, RBAC, rate limiting, and privacy controls.

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
