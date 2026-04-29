# Admin Moderation And Audit

## Purpose

The MVP admin moderation and audit foundation records important trust and safety actions so Ultron Sport can later support compliance, accountability, abuse investigation, and platform trust.

## Current Scope

- ADMIN users can search audit logs at `/api/admin/audit-logs`.
- ADMIN users can view flagged evidence at `/api/admin/moderation/evidence/flagged`.
- ADMIN users can view archived evidence at `/api/admin/moderation/evidence/archived`.
- ADMIN users can add moderation notes at `/api/admin/moderation/evidence/{evidenceId}/note`.
- Evidence flag/archive actions create AdminActionLog records.
- Admin LevelPlay recalculation actions create AdminActionLog records.
- Audit logs are append-only through the service/API surface.

## Audit Log Fields

- adminUserId
- adminEmail
- adminDisplayName
- actionType
- targetType
- targetId
- reason
- details
- createdAt

## MVP Boundaries

The MVP does not implement enterprise SIEM, Kafka/event streaming, Redis, external logging platforms, automated fraud detection, AI moderation, or complex dashboards.

Viewing verification history is not logged yet to avoid noisy audit trails. The MVP logs state-changing moderation and score administration actions first.

Future work can add request IP, user agent, immutable database constraints, export workflows, moderation assignment queues, and POPIA/privacy reporting.
