# Architecture Specification

## Overview

Ultron Sport should be implemented as a mobile-first application supported by a web portal and a modular backend. The long-term direction is a microservices architecture, while the MVP may start as a modular monolith if that reduces delivery risk. Service boundaries should still be respected so the system can evolve cleanly.

## Recommended Components

### Client Layer

- Mobile app for athletes, coaches, fans, scouts, and agents
- Web portal for administrators, institutions, scouts, analytics, and content review
- Responsive views for desktop features such as analytics, streaming, and organization dashboards

### API Gateway

- Routes client requests to backend services
- Handles authentication checks, rate limiting, request validation, and protocol translation
- Provides a single public entry point over HTTPS

### Core Services

- Auth Service: registration, login, sessions, MFA, account recovery, role assignment
- Profile Service: athlete, coach, scout, institution, and fan profile data
- Evidence Service: uploads, metadata, file hashes, evidence lifecycle, verification requests
- Verification Service: coach and institution approvals, rejections, flags, audit logs
- Ranking Service: LevelPlay Rank calculation, rank history, tiers, explainability
- Search Service: athlete search, filters, saved searches, indexing
- Notification Service: push, email, SMS, in-app notification feed
- Messaging and Offers Service: recruiter communication, invitations, offer workflows
- Admin Service: moderation, role management, flagged content, compliance workflows
- Analytics Service: dashboards, metrics, reporting, future model insights
- AI Engine: future video analysis, metric extraction, recommendations, model versioning

## Data Stores

- Relational database, such as PostgreSQL, for users, profiles, evidence metadata, verification records, ratings, offers, and audit logs
- Object storage, such as AWS S3 or Azure Blob, for videos and documents
- Search index, such as OpenSearch or Elasticsearch, for full-text and filtered discovery
- Cache, such as Redis, for sessions, frequently accessed rankings, and direct lookups
- Message queue, such as Kafka, RabbitMQ, or a managed cloud queue, for asynchronous workflows

## Evidence Upload and Verification Flow

1. Athlete uploads video or supporting evidence from the mobile app.
2. API Gateway sends the request to the Evidence Service.
3. Evidence Service stores the file in object storage and records metadata.
4. Evidence Service calculates or stores file hash and sets status to pending verification.
5. AI Engine may process the evidence asynchronously when enabled.
6. Coach or institution receives a verification request.
7. Verifier approves, rejects, or flags the evidence.
8. Ranking Service updates LevelPlay Rank only when evidence is verified.
9. Notification Service informs the athlete of the result and any ranking change.

## Evidence Lifecycle

```text
Draft -> Submitted -> Pending Verification -> Verified -> Ranked
                          |                       |
                          v                       v
                       Rejected                Archived
                          |
                          v
                        Flagged -> Admin Review -> Verified/Rejected/Archived
```

## Deployment Direction

- Host public clients behind HTTPS and a CDN.
- Keep databases and object storage in private network zones.
- Deploy services as containers.
- Use managed identity and least-privilege access between services.
- Add observability from the start: logs, metrics, traces, alerts, and audit logs.
- Use CI/CD to run tests and deploy repeatably.

## Architecture Decisions

- MVP can start as a modular monolith if the team is small, but modules must map to future services.
- Evidence upload and video processing must be asynchronous.
- Ranking logic must be isolated behind a service or module API so formulas can change safely.
- Search should use database indexes for the MVP and move to OpenSearch/Elasticsearch when data volume grows.
- AI should not block MVP launch; the MVP should capture clean metadata for later AI models.
