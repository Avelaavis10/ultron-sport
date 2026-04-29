# API Endpoints Draft

MVP API base path:

```text
/api/v1
```

Authentication base path:

```text
/api/auth
```

Evidence workflow base path:

```text
/api/evidence
```

Discovery base path:

```text
/api/discovery
```

LevelPlay base path:

```text
/api/levelplay
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
| POST | `/` | ATHLETE | Create DRAFT evidence metadata with `fileUrl` or `externalVideoLink` |
| GET | `/{id}` | Owner ATHLETE, COACH for pending, ADMIN, SCOUT_AGENT/ORGANISATION for VERIFIED | Get evidence by visibility rules |
| GET | `/my` | ATHLETE | List evidence owned by the current athlete |
| PATCH | `/{id}` | ATHLETE | Update own evidence only while DRAFT or REJECTED |
| POST | `/{id}/submit` | ATHLETE | Move DRAFT or REJECTED evidence to PENDING_VERIFICATION |
| GET | `/pending-verification` | COACH, ADMIN | List evidence awaiting verification |
| POST | `/{id}/verify` | COACH | Mark pending evidence as VERIFIED |
| POST | `/{id}/reject` | COACH | Reject pending evidence with a required reason |
| POST | `/{id}/flag` | ADMIN | Flag evidence with a required reason |
| POST | `/{id}/archive` | ADMIN | Archive evidence |
| GET | `/{id}/verification-history` | ADMIN | View simple verification history |

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
| GET | `/me` | ATHLETE | Get the current athlete's LevelPlay score |
| GET | `/athletes/{athleteProfileId}` | Authenticated | Get an athlete's current LevelPlay score |
| GET | `/athletes/{athleteProfileId}/explain` | Authenticated | Get a transparent score breakdown |
| POST | `/athletes/{athleteProfileId}/recalculate` | ADMIN | Recalculate one athlete's score |
| POST | `/recalculate-all` | ADMIN | Recalculate all athlete scores with a simple MVP loop |

Legacy `/api/v1/levelplay-scores/...` endpoints remain for compatibility, but new clients should use `/api/levelplay`.

## Discovery

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| GET | `/athletes` | Authenticated | Search athlete discovery cards with role-aware evidence visibility |
| GET | `/athletes/{athleteProfileId}` | Authenticated | View an athlete discovery profile with visible evidence and summaries |
| GET | `/evidence` | Authenticated | Search evidence discovery cards with pagination and filters |

Supported discovery filters:

```text
sport, position, location, organisationId, verificationStatus,
minLevelPlayScore, maxLevelPlayScore, tier, hasVerifiedEvidence,
keyword, page, size, sortBy, sortDirection
```

Defaults: `page=0`, `size=20`, `sortBy=updatedAt`, `sortDirection=DESC`. Maximum `size` is `50`.

## Admin Moderation

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/admin/actions` | ADMIN | Log admin/moderation action |
| GET | `/admin/actions/admin/{adminUserId}` | ADMIN | List actions performed by admin |

## API Contract Notes

- Request DTOs use validation annotations.
- Response DTOs avoid exposing password hashes.
- Protected endpoints require `Authorization: Bearer <accessToken>`.
- Evidence currently accepts a `fileUrl` or `externalVideoLink`; binary file upload can be added later behind scanning and storage controls.
- Evidence AI status defaults to `NOT_STARTED`; no AI service is called in the MVP workflow.
- Discovery is relational database search for the MVP. Elasticsearch/OpenSearch, caching, vector search, and recommendation ranking are future work.
- LevelPlay Rank uses verified evidence, achievements, coach verification count, and profile completeness only. Popularity, fan votes, views, likes, paid boosts, and AI scoring are not part of the MVP formula.
