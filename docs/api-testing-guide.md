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
