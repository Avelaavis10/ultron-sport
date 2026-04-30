# API Testing Guide

## Base URL

```text
http://localhost:8080
```

## Health Smoke Test

```http
GET /api/health
```

## Auth Flow

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

Use the token:

```http
Authorization: Bearer <accessToken>
```

## Endpoint Groups

- Auth: `/api/auth`
- Athlete profiles: `/api/athlete-profiles`
- Achievements: `/api/achievements`
- Evidence: `/api/evidence`
- Media: `/api/media`
- Discovery: `/api/discovery`
- LevelPlay: `/api/levelplay`
- Organisations: `/api/organisations`
- Coach profiles: `/api/coach-profiles`
- Admin moderation/audit: `/api/admin`
- Notifications: `/api/notifications`
- Health: `/api/health`

## Manual Request Collection

Use the checked-in HTTP collection for a full manual MVP flow:

```text
docs/http/ultron-sport-mvp.http
```

The collection includes:

- Health smoke checks.
- Registration and login for ADMIN, ATHLETE, COACH, ORGANISATION, and SCOUT_AGENT.
- Organisation, athlete profile, coach profile, achievement, URL-only evidence, evidence submission, coach verification, LevelPlay, discovery, notifications, and admin moderation requests.
- Negative tests for validation, 401, 403, 404, invalid enum/status, page-size limits, and role violations.

Use `docs/manual-testing-seed-data.md` for sample payloads and the recommended request order. Use `docs/role-endpoint-access-matrix.md` when a request returns `403`.

## Example Error

```http
POST /api/auth/register
Content-Type: application/json

{
  "displayName": "",
  "email": "not-an-email",
  "password": "",
  "role": null
}
```

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed.",
  "path": "/api/auth/register",
  "code": "VALIDATION_FAILED",
  "traceId": "...",
  "validationErrors": {
    "email": "must be a well-formed email address"
  }
}
```
