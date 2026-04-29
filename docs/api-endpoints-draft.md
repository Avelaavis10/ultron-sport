# API Endpoints Draft

MVP API base path:

```text
/api/v1
```

Authentication base path:

```text
/api/auth
```

## Authentication

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/register` | Public | Register a user and return a JWT bearer token |
| POST | `/login` | Public | Authenticate with email/password and return a JWT bearer token |
| GET | `/me` | Authenticated | Return the current authenticated user |

## Users

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| GET | `/users` | ADMIN | List users |
| GET | `/users/{id}` | ADMIN | Get one user |

## Athlete Profiles

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/athlete-profiles` | ATHLETE | Create athlete profile |
| GET | `/athlete-profiles/{id}` | Authenticated | Get athlete profile |
| GET | `/athlete-profiles?sport=&location=&position=` | COACH, ORGANISATION, SCOUT_AGENT, ADMIN | Search/filter athlete profiles |

## Coach Profiles

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/coach-profiles` | COACH | Create coach profile |
| GET | `/coach-profiles/{id}` | COACH, ADMIN | Get coach profile |

## Organisations

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/organisations` | ORGANISATION, ADMIN | Create school, club, or organisation record |
| GET | `/organisations/{id}` | ORGANISATION, ADMIN | Get organisation record |

## Evidence

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/evidence` | ATHLETE | Submit evidence metadata with file URL or external link |
| GET | `/evidence/{id}` | Authenticated | Get evidence |
| GET | `/evidence/athlete/{athleteProfileId}` | Authenticated | List evidence for athlete profile |

## Verification Requests

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/verification-requests` | ATHLETE | Request coach or organisation verification |
| GET | `/verification-requests/{id}` | COACH, ADMIN | Get verification request |
| POST | `/verification-requests/{id}/approve` | COACH, ADMIN | Approve evidence |
| POST | `/verification-requests/{id}/reject` | COACH, ADMIN | Reject evidence |
| POST | `/verification-requests/{id}/flag` | COACH, ADMIN | Flag evidence for moderation |

## Achievements

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/achievements` | ATHLETE | Add athlete achievement |
| GET | `/achievements/athlete/{athleteProfileId}` | Authenticated | List achievements for athlete profile |

## LevelPlay Scores

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| GET | `/levelplay-scores/athlete/{athleteProfileId}` | Authenticated | Get or create placeholder score |
| POST | `/levelplay-scores/athlete/{athleteProfileId}/refresh` | Authenticated | Refresh placeholder score from current MVP data |

## Admin Moderation

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/admin/actions` | ADMIN | Log admin/moderation action |
| GET | `/admin/actions/admin/{adminUserId}` | ADMIN | List actions performed by admin |

## API Contract Notes

- Request DTOs use validation annotations.
- Response DTOs avoid exposing password hashes.
- Protected endpoints require `Authorization: Bearer <accessToken>`.
- Evidence currently accepts a file URL or external link; binary file upload can be added later behind scanning and storage controls.
