# Ultron Sport MVP API Contract

This document is the developer-facing MVP API contract. It reflects the current Spring Boot backend and is intended for manual testing, frontend/mobile planning, and handover.

Base URL:

```text
http://localhost:8080
```

Primary API base path:

```text
/api
```

Legacy compatibility note: some controllers still expose `/api/v1/...` paths from earlier foundation work. New clients should use the `/api/...` paths documented below unless a legacy endpoint is explicitly called out.

## Authentication Pattern

Public endpoints are explicitly marked. Protected endpoints require:

```http
Authorization: Bearer <accessToken>
```

Register:

```http
POST /api/auth/register
Content-Type: application/json

{
  "displayName": "Test Athlete",
  "email": "athlete@example.com",
  "phone": null,
  "password": "password123",
  "role": "ATHLETE"
}
```

Login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "athlete@example.com",
  "password": "password123"
}
```

Auth response:

```json
{
  "tokenType": "Bearer",
  "accessToken": "...",
  "userId": 2,
  "displayName": "Test Athlete",
  "email": "athlete@example.com",
  "role": "ATHLETE"
}
```

## Standard Errors

All API errors use the same JSON shape:

```json
{
  "timestamp": "2026-04-30T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed.",
  "path": "/api/auth/register",
  "code": "VALIDATION_FAILED",
  "traceId": "uuid",
  "validationErrors": {
    "email": "must be a well-formed email address"
  }
}
```

Common responses:

| Status | Meaning |
| --- | --- |
| 400 | Validation failed, malformed JSON, invalid enum, invalid request, or invalid workflow transition |
| 401 | Missing, invalid, or expired bearer token |
| 403 | Authenticated user does not have the required role or ownership |
| 404 | Requested resource was not found or is not visible to the caller |
| 405 | HTTP method is not allowed |
| 409 | Duplicate resource |
| 415 | Unsupported media type |
| 500 | Unexpected server error |

## Health

| Method | Path | Auth | Roles | Purpose |
| --- | --- | --- | --- | --- |
| GET | `/api/health` | No | PUBLIC | Simple application health |
| GET | `/api/health/readiness` | No | PUBLIC | MVP readiness with database status |
| GET | `/api/health/version` | No | PUBLIC | Application name, version, and environment |

Example:

```http
GET /api/health
```

```json
{
  "status": "UP",
  "application": "Ultron Sport API",
  "environment": "local",
  "timestamp": "2026-04-30T00:00:00Z"
}
```

Common errors: readiness may return `503` if the database connectivity check fails.

## Auth

| Method | Path | Auth | Roles | Purpose |
| --- | --- | --- | --- | --- |
| POST | `/api/auth/register` | No | PUBLIC | Register a user and return a bearer token |
| POST | `/api/auth/login` | No | PUBLIC | Login with email/password and return a bearer token |
| GET | `/api/auth/me` | Yes | Any authenticated user | Return current user details |

Allowed MVP roles for normal testing: `ATHLETE`, `COACH`, `ORGANISATION`, `SCOUT_AGENT`, `ADMIN`.

Common errors: `400` validation, `401` bad credentials or missing token, `409` duplicate email.

## Athlete Profiles

| Method | Path | Auth | Roles | Purpose |
| --- | --- | --- | --- | --- |
| POST | `/api/athlete-profiles` | Yes | ATHLETE | Create the current user's athlete profile |
| GET | `/api/athlete-profiles/me` | Yes | ATHLETE | Get the current athlete's full profile |
| PATCH | `/api/athlete-profiles/me` | Yes | ATHLETE | Update own profile and recalculate LevelPlay |
| PATCH | `/api/athlete-profiles/me/organisation` | Yes | ATHLETE | Link own profile to an organisation or school/club fallback |
| GET | `/api/athlete-profiles/{athleteProfileId}` | Yes | ADMIN, COACH, owning ATHLETE | Get internal athlete profile view |
| GET | `/api/athlete-profiles/{athleteProfileId}/achievements` | Yes | ADMIN, COACH, owning ATHLETE | List achievements for an athlete profile |
| GET | `/api/athlete-profiles` | Yes | ADMIN | Paginated athlete profile list |

Create profile:

```http
POST /api/athlete-profiles
Authorization: Bearer <athleteToken>
Content-Type: application/json

{
  "sport": "Football",
  "position": "Forward",
  "age": 19,
  "gender": "Female",
  "location": "Cape Town",
  "schoolOrClub": "Ultron Academy",
  "organisationId": null,
  "bio": "Fast winger with verified match evidence."
}
```

Common errors: `400` validation, `401` missing token, `403` wrong role or ownership failure, `409` duplicate athlete profile.

Legacy compatibility: `/api/v1/athlete-profiles` maps to the same controller and remains for older MVP tests.

## Achievements

| Method | Path | Auth | Roles | Purpose |
| --- | --- | --- | --- | --- |
| POST | `/api/achievements` | Yes | ATHLETE | Create an achievement for own athlete profile |
| GET | `/api/achievements/my` | Yes | ATHLETE | List current athlete's achievements |
| PATCH | `/api/achievements/{achievementId}` | Yes | Owning ATHLETE | Update own achievement |
| GET | `/api/achievements` | Yes | ADMIN | Paginated achievement list |
| GET | `/api/achievements/athlete/{athleteProfileId}` | Yes | ADMIN, COACH, owning ATHLETE | Legacy-style achievement list path |

Create achievement:

```http
POST /api/achievements
Authorization: Bearer <athleteToken>
Content-Type: application/json

{
  "athleteProfileId": 1,
  "title": "Regional Top Scorer",
  "description": "Top scorer in the under-19 regional tournament.",
  "achievedAt": "2024-09-14"
}
```

Common errors: `400` validation, `403` wrong role or ownership failure, `404` athlete profile or achievement not found.

Legacy compatibility: `/api/v1/achievements` maps to the same controller and remains for older MVP tests.

## Organisations

| Method | Path | Auth | Roles | Purpose |
| --- | --- | --- | --- | --- |
| POST | `/api/organisations` | Yes | ADMIN, ORGANISATION | Create an organisation record |
| GET | `/api/organisations` | Yes | Any authenticated user | Search organisations |
| GET | `/api/organisations/{organisationId}` | Yes | Any authenticated user | Get one organisation |
| PATCH | `/api/organisations/{organisationId}` | Yes | ADMIN | Update organisation details or verification status |

Create organisation:

```http
POST /api/organisations
Authorization: Bearer <adminToken>
Content-Type: application/json

{
  "name": "Ultron Football Academy",
  "type": "ACADEMY",
  "location": "Cape Town",
  "contactEmail": "admin@ultronacademy.example",
  "primaryAdminUserId": null
}
```

Search filters: `name`, `type`, `location`, `verificationStatus`, `page`, `size`, `sortBy`, `sortDirection`.

Common errors: `400` validation or invalid enum, `403` wrong role for create/update, `404` organisation not found.

Legacy compatibility: `/api/v1/organisations` maps to the same controller and remains for older MVP tests.

## Coach Profiles

| Method | Path | Auth | Roles | Purpose |
| --- | --- | --- | --- | --- |
| POST | `/api/coach-profiles` | Yes | COACH | Create current coach profile |
| GET | `/api/coach-profiles/me` | Yes | COACH | Get current coach profile |
| PATCH | `/api/coach-profiles/me` | Yes | COACH | Update current coach profile |
| GET | `/api/coach-profiles/{coachProfileId}` | Yes | ADMIN, owning COACH | Get coach profile |

Create coach profile:

```http
POST /api/coach-profiles
Authorization: Bearer <coachToken>
Content-Type: application/json

{
  "certificationReference": "SAFA-D-12345",
  "organisationId": 1,
  "organisationName": "Ultron Football Academy",
  "sport": "Football",
  "qualificationSummary": "Youth development coach.",
  "yearsExperience": 6
}
```

Important MVP rule: a COACH must have a CoachProfile before verifying or rejecting evidence.

Common errors: `400` validation, `403` wrong role or ownership failure, `409` duplicate coach profile.

Legacy compatibility: `/api/v1/coach-profiles` maps to the same controller and remains for older MVP tests.

## Evidence

| Method | Path | Auth | Roles | Purpose |
| --- | --- | --- | --- | --- |
| POST | `/api/evidence` | Yes | ATHLETE | Create DRAFT evidence metadata |
| GET | `/api/evidence/{evidenceId}` | Yes | Owner ATHLETE, COACH for pending, ADMIN, SCOUT_AGENT/ORGANISATION for VERIFIED | Get evidence by role visibility |
| GET | `/api/evidence/my` | Yes | ATHLETE | List current athlete's evidence |
| PATCH | `/api/evidence/{evidenceId}` | Yes | Owning ATHLETE | Update own DRAFT or REJECTED evidence |
| POST | `/api/evidence/{evidenceId}/media/{mediaId}` | Yes | Owning ATHLETE | Attach own media to editable evidence |
| POST | `/api/evidence/{evidenceId}/submit` | Yes | Owning ATHLETE | Submit evidence for verification |
| GET | `/api/evidence/pending-verification` | Yes | COACH, ADMIN | List evidence awaiting verification |
| POST | `/api/evidence/{evidenceId}/verify` | Yes | COACH | Verify pending evidence |
| POST | `/api/evidence/{evidenceId}/reject` | Yes | COACH | Reject pending evidence with a reason |
| POST | `/api/evidence/{evidenceId}/flag` | Yes | ADMIN | Flag evidence with a reason |
| POST | `/api/evidence/{evidenceId}/archive` | Yes | ADMIN | Archive evidence |
| GET | `/api/evidence/{evidenceId}/verification-history` | Yes | ADMIN | View verification history |
| GET | `/api/evidence/{evidenceId}/verification-context` | Yes | COACH, ADMIN | View coach, athlete, and organisation context |

Create URL-only evidence:

```http
POST /api/evidence
Authorization: Bearer <athleteToken>
Content-Type: application/json

{
  "athleteProfileId": 1,
  "title": "Two goals against City FC",
  "description": "Match clip with goals and pressing actions.",
  "sport": "Football",
  "position": "Forward",
  "eventType": "League match",
  "matchOrTraining": "MATCH",
  "eventDate": "2024-09-21",
  "fileUrl": null,
  "externalVideoLink": "https://video.example/evidence/two-goals"
}
```

Reject evidence:

```http
POST /api/evidence/1/reject
Authorization: Bearer <coachToken>
Content-Type: application/json

{
  "reason": "Clip does not show the claimed event clearly."
}
```

Common errors: `400` validation or invalid transition, `403` wrong role or ownership failure, `404` evidence not found or not visible.

MVP notes: evidence uses URL-only mode or attached local/mock media. Direct production object storage, CDN, scanning, thumbnails, transcoding, chunked upload, and AI analysis are deferred.

## Media

| Method | Path | Auth | Roles | Purpose |
| --- | --- | --- | --- | --- |
| POST | `/api/media/upload?athleteProfileId={id}` | Yes | ATHLETE | Upload supported media to local/mock storage |
| GET | `/api/media/{mediaId}` | Yes | Owner ATHLETE, ADMIN | Get media metadata without internal paths |

Supported content types: `video/mp4`, `video/quicktime`, `image/jpeg`, `image/png`.

Default max size: 50MB.

Multipart curl example:

```bash
curl -X POST "http://localhost:8080/api/media/upload?athleteProfileId=1" \
  -H "Authorization: Bearer <athleteToken>" \
  -F "file=@/path/to/sample.mp4;type=video/mp4"
```

Common errors: `400` empty file or unsupported file, `403` wrong role or ownership failure, `404` media not found, `415` unsupported request media type.

## Discovery

| Method | Path | Auth | Roles | Purpose |
| --- | --- | --- | --- | --- |
| GET | `/api/discovery/athletes` | Yes | ATHLETE, COACH, ORGANISATION, SCOUT_AGENT, ADMIN | Search athlete discovery cards |
| GET | `/api/discovery/athletes/{athleteProfileId}` | Yes | ATHLETE, COACH, ORGANISATION, SCOUT_AGENT, ADMIN | View discovery-safe athlete profile |
| GET | `/api/discovery/evidence` | Yes | ATHLETE, COACH, ORGANISATION, SCOUT_AGENT, ADMIN | Search visible evidence cards |

Filters: `sport`, `position`, `location`, `organisationId`, `verificationStatus`, `minLevelPlayScore`, `maxLevelPlayScore`, `tier`, `hasVerifiedEvidence`, `keyword`, `page`, `size`, `sortBy`, `sortDirection`.

Example:

```http
GET /api/discovery/athletes?sport=Football&position=Forward&hasVerifiedEvidence=true&page=0&size=20
Authorization: Bearer <scoutToken>
```

Visibility rules: SCOUT_AGENT and ORGANISATION users see VERIFIED evidence only. ADMIN can filter all statuses. ATHLETE users can view own full profile through profile endpoints and discovery-safe public/verified data for other athletes.

Common errors: `400` invalid filter, invalid sort, or size above 50; `401` missing token; `403` role restriction if added later.

## LevelPlay

| Method | Path | Auth | Roles | Purpose |
| --- | --- | --- | --- | --- |
| GET | `/api/levelplay/me` | Yes | ATHLETE | Get current athlete score |
| GET | `/api/levelplay/athletes/{athleteProfileId}` | Yes | ATHLETE, COACH, ORGANISATION, SCOUT_AGENT, ADMIN | Get athlete score |
| GET | `/api/levelplay/athletes/{athleteProfileId}/explain` | Yes | ATHLETE, COACH, ORGANISATION, SCOUT_AGENT, ADMIN | Get transparent score breakdown |
| POST | `/api/levelplay/athletes/{athleteProfileId}/recalculate` | Yes | ADMIN | Recalculate one athlete score |
| POST | `/api/levelplay/recalculate-all` | Yes | ADMIN | Recalculate all athlete scores |

Example:

```http
GET /api/levelplay/athletes/1/explain
Authorization: Bearer <scoutToken>
```

MVP scoring uses verified evidence count, achievement count, coach verification count, and profile completeness only. Popularity, likes, views, fan votes, paid boosts, and AI scoring are not used.

Common errors: `403` non-admin recalculation, `404` score or athlete not found.

Legacy compatibility: `/api/v1/levelplay-scores/...` remains for earlier tests. New clients should use `/api/levelplay`.

## Admin Moderation And Audit

| Method | Path | Auth | Roles | Purpose |
| --- | --- | --- | --- | --- |
| GET | `/api/admin/audit-logs` | Yes | ADMIN | Search paginated audit logs |
| GET | `/api/admin/audit-logs/{id}` | Yes | ADMIN | Get one audit log |
| GET | `/api/admin/audit-logs/target/{targetType}/{targetId}` | Yes | ADMIN | List logs for a target |
| GET | `/api/admin/moderation/evidence/flagged` | Yes | ADMIN | List flagged evidence |
| GET | `/api/admin/moderation/evidence/archived` | Yes | ADMIN | List archived evidence |
| POST | `/api/admin/moderation/evidence/{evidenceId}/note` | Yes | ADMIN | Add append-only moderation note |
| GET | `/api/admin/moderation/summary` | Yes | ADMIN | Return moderation counts |

Audit filters: `actionType`, `targetType`, `targetId`, `adminUserId`, `fromDate`, `toDate`, `page`, `size`, `sortBy`, `sortDirection`.

Create moderation note:

```http
POST /api/admin/moderation/evidence/1/note
Authorization: Bearer <adminToken>
Content-Type: application/json

{
  "reason": "Manual review",
  "details": "Evidence reviewed during MVP manual testing."
}
```

Common errors: `400` invalid enum/filter or size above 50, `403` non-admin access, `404` audit log/evidence not found.

## Notifications

| Method | Path | Auth | Roles | Purpose |
| --- | --- | --- | --- | --- |
| GET | `/api/notifications` | Yes | Any authenticated user | List current user's notifications |
| GET | `/api/notifications/unread` | Yes | Any authenticated user | List current user's unread notifications |
| GET | `/api/notifications/unread-count` | Yes | Any authenticated user | Count unread notifications |
| POST | `/api/notifications/{notificationId}/read` | Yes | Owning authenticated user | Mark one notification read |
| POST | `/api/notifications/read-all` | Yes | Any authenticated user | Mark all owned notifications read |

Filters: `status`, `page`, `size`, `sortBy`, `sortDirection`.

Example:

```http
GET /api/notifications?status=UNREAD&page=0&size=20
Authorization: Bearer <athleteToken>
```

Common errors: `400` invalid status/sort/size, `403` ownership failure, `404` notification not found.

MVP notes: notifications are in-app and database-backed only. Email, SMS, push, WebSockets, external queues, and preferences are deferred.

## Legacy Admin/User/Verification Request Endpoints

These endpoints remain for compatibility with earlier MVP foundation slices and tests. New feature work should prefer the current `/api/...` modules above.

| Method | Path | Auth | Roles | Purpose |
| --- | --- | --- | --- | --- |
| GET | `/api/v1/users` | Yes | ADMIN | List users |
| GET | `/api/v1/users/{id}` | Yes | ADMIN | Get one user |
| POST | `/api/v1/verification-requests` | Yes | ATHLETE | Create legacy verification request |
| GET | `/api/v1/verification-requests/{id}` | Yes | COACH, ADMIN | Get legacy verification request |
| POST | `/api/v1/verification-requests/{id}/approve` | Yes | COACH, ADMIN | Approve legacy verification request |
| POST | `/api/v1/verification-requests/{id}/reject` | Yes | COACH, ADMIN | Reject legacy verification request |
| POST | `/api/v1/verification-requests/{id}/flag` | Yes | COACH, ADMIN | Flag legacy verification request |

## API Contract Notes

- Current user endpoints use `/me`.
- Admin-only routes sit under `/api/admin/**`.
- Collection routes use plural nouns where practical.
- Pagination responses use `content`, `page`, `size`, `totalElements`, `totalPages`, `sortBy`, and `sortDirection`.
- DTOs do not expose password hashes, JWT internals, raw local filesystem paths, or sensitive security data.
- Discovery responses are role-aware and do not expose unverified evidence to scouts or organisations.
- Audit logs and notifications are append-only through the public API surface.
- The MVP does not implement AI, production object storage, enterprise observability, email/SMS/push, frontend/mobile UI, Docker/Kubernetes, or CI/CD in this slice.
