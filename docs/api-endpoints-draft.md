# API Endpoints Draft

Base path:

```text
/api/v1
```

## Authentication

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/auth/register` | Register a user with a role-ready account |

## Users

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/users` | List users |
| GET | `/users/{id}` | Get one user |

## Athlete Profiles

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/athlete-profiles` | Create athlete profile |
| GET | `/athlete-profiles/{id}` | Get athlete profile |
| GET | `/athlete-profiles?sport=&location=&position=` | Search/filter athlete profiles |

## Coach Profiles

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/coach-profiles` | Create coach profile |
| GET | `/coach-profiles/{id}` | Get coach profile |

## Organisations

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/organisations` | Create school, club, or organisation record |
| GET | `/organisations/{id}` | Get organisation record |

## Evidence

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/evidence` | Submit evidence metadata with file URL or external link |
| GET | `/evidence/{id}` | Get evidence |
| GET | `/evidence/athlete/{athleteProfileId}` | List evidence for athlete profile |

## Verification Requests

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/verification-requests` | Request coach or organisation verification |
| GET | `/verification-requests/{id}` | Get verification request |
| POST | `/verification-requests/{id}/approve` | Approve evidence |
| POST | `/verification-requests/{id}/reject` | Reject evidence |
| POST | `/verification-requests/{id}/flag` | Flag evidence for moderation |

## Achievements

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/achievements` | Add athlete achievement |
| GET | `/achievements/athlete/{athleteProfileId}` | List achievements for athlete profile |

## LevelPlay Scores

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/levelplay-scores/athlete/{athleteProfileId}` | Get or create placeholder score |
| POST | `/levelplay-scores/athlete/{athleteProfileId}/refresh` | Refresh placeholder score from current MVP data |

## Admin Moderation

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/admin/actions` | Log admin/moderation action |
| GET | `/admin/actions/admin/{adminUserId}` | List actions performed by admin |

## API Contract Notes

- Request DTOs use validation annotations.
- Response DTOs avoid exposing password hashes.
- Authentication and role enforcement are intentionally staged.
- Evidence currently accepts a file URL or external link; binary file upload can be added later behind scanning and storage controls.
