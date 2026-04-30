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

Admin base path:

```text
/api/admin
```

Media base path:

```text
/api/media
```

Notifications base path:

```text
/api/notifications
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
| POST | `/api/athlete-profiles` | ATHLETE | Create the current user's athlete profile |
| GET | `/api/athlete-profiles/me` | ATHLETE | Get the current athlete's full profile |
| PATCH | `/api/athlete-profiles/me` | ATHLETE | Update the current athlete's profile and recalculate LevelPlay |
| PATCH | `/api/athlete-profiles/me/organisation` | ATHLETE | Link current athlete profile to an existing organisation or update school/club fallback text |
| GET | `/api/athlete-profiles/{athleteProfileId}` | ADMIN, COACH, owning ATHLETE | Get full internal profile view |
| GET | `/api/athlete-profiles` | ADMIN | Paginated list of athlete profiles |
| GET | `/api/v1/athlete-profiles?sport=&location=&position=` | Legacy authenticated roles | Legacy profile search/filter path |

## Coach Profiles

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/api/coach-profiles` | COACH | Create current coach user's profile; duplicate coach profiles are rejected |
| GET | `/api/coach-profiles/me` | COACH | Get current coach profile |
| PATCH | `/api/coach-profiles/me` | COACH | Update current coach profile and organisation link |
| GET | `/api/coach-profiles/{id}` | Owning COACH, ADMIN | Get coach profile |

## Organisations

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/api/organisations` | ORGANISATION, ADMIN | Create school, club, academy, university, team, or organisation record |
| GET | `/api/organisations` | Authenticated | Search organisations by name, type, location, or verification status |
| GET | `/api/organisations/{id}` | Authenticated | Get organisation record |
| PATCH | `/api/organisations/{id}` | ADMIN | Update organisation details or verification status |

## Evidence

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/` | ATHLETE | Create DRAFT evidence metadata with `fileUrl` or `externalVideoLink` |
| GET | `/{id}` | Owner ATHLETE, COACH for pending, ADMIN, SCOUT_AGENT/ORGANISATION for VERIFIED | Get evidence by visibility rules |
| GET | `/my` | ATHLETE | List evidence owned by the current athlete |
| PATCH | `/{id}` | ATHLETE | Update own evidence only while DRAFT or REJECTED |
| POST | `/{id}/media/{mediaId}` | ATHLETE | Attach own uploaded media to own DRAFT or REJECTED evidence |
| POST | `/{id}/submit` | ATHLETE | Move DRAFT or REJECTED evidence to PENDING_VERIFICATION |
| GET | `/pending-verification` | COACH, ADMIN | List evidence awaiting verification |
| POST | `/{id}/verify` | COACH | Mark pending evidence as VERIFIED |
| POST | `/{id}/reject` | COACH | Reject pending evidence with a required reason |
| POST | `/{id}/flag` | ADMIN | Flag evidence with a required reason |
| POST | `/{id}/archive` | ADMIN | Archive evidence |
| GET | `/{id}/verification-history` | ADMIN | View simple verification history |
| GET | `/{id}/verification-context` | COACH, ADMIN | View athlete, coach, organisation, and shared-context details for an evidence item |

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
| POST | `/api/achievements` | ATHLETE | Add achievement for own athlete profile and recalculate LevelPlay |
| GET | `/api/achievements/my` | ATHLETE | List current athlete's achievements |
| PATCH | `/api/achievements/{achievementId}` | Owning ATHLETE | Update own unverified achievement and recalculate LevelPlay |
| GET | `/api/achievements` | ADMIN | Paginated list of achievements |
| GET | `/api/athlete-profiles/{athleteProfileId}/achievements` | ADMIN, COACH, owning ATHLETE | List achievements for a profile |
| GET | `/api/v1/achievements/athlete/{athleteProfileId}` | Legacy authenticated roles | Legacy achievement list path |

## LevelPlay Scores

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| GET | `/me` | ATHLETE | Get the current athlete's LevelPlay score |
| GET | `/athletes/{athleteProfileId}` | Authenticated | Get an athlete's current LevelPlay score |
| GET | `/athletes/{athleteProfileId}/explain` | Authenticated | Get a transparent score breakdown |
| POST | `/athletes/{athleteProfileId}/recalculate` | ADMIN | Recalculate one athlete's score |
| POST | `/recalculate-all` | ADMIN | Recalculate all athlete scores with a simple MVP loop |

Legacy `/api/v1/levelplay-scores/...` endpoints remain for compatibility, but new clients should use `/api/levelplay`.

## Media

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/upload` | ATHLETE | Upload supported media to local/mock storage and return `mediaId` plus `publicUrl` |
| GET | `/{mediaId}` | Owner ATHLETE, ADMIN | Return media metadata without exposing internal storage paths |

Supported MVP upload content types are `video/mp4`, `video/quicktime`, `image/jpeg`, and `image/png`. The default max upload size is 50MB. Media scan status defaults to `SKIPPED_FOR_MVP`; malware scanning, object storage, CDN URLs, thumbnails, transcoding, chunked upload, and AI analysis are future work.

## Notifications

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| GET | `/api/notifications` | Authenticated | List the current user's notifications with optional status filter, pagination, and sorting |
| GET | `/api/notifications/unread` | Authenticated | List unread notifications for the current user |
| GET | `/api/notifications/unread-count` | Authenticated | Return the current user's unread notification count |
| POST | `/api/notifications/{notificationId}/read` | Owning authenticated user | Mark one owned notification as read |
| POST | `/api/notifications/read-all` | Authenticated | Mark all current-user notifications as read |

Supported filters:

```text
status, page, size, sortBy, sortDirection
```

Defaults: `page=0`, `size=20`, `sortBy=createdAt`, `sortDirection=DESC`. Maximum `size` is `50`. Notification responses do not expose raw internal metadata.

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
| GET | `/audit-logs` | ADMIN | Search paginated audit logs with filters |
| GET | `/audit-logs/{id}` | ADMIN | Get one audit log |
| GET | `/audit-logs/target/{targetType}/{targetId}` | ADMIN | List logs for a specific target |
| GET | `/moderation/evidence/flagged` | ADMIN | List flagged evidence |
| GET | `/moderation/evidence/archived` | ADMIN | List archived evidence |
| POST | `/moderation/evidence/{evidenceId}/note` | ADMIN | Add an append-only moderation note |
| GET | `/moderation/summary` | ADMIN | Return MVP moderation counts |

Supported audit log filters:

```text
actionType, targetType, targetId, adminUserId, fromDate, toDate,
page, size, sortBy, sortDirection
```

Defaults: `page=0`, `size=20`, `sortBy=createdAt`, `sortDirection=DESC`. Maximum `size` is `50`.

## API Contract Notes

- Request DTOs use validation annotations.
- Response DTOs avoid exposing password hashes.
- Protected endpoints require `Authorization: Bearer <accessToken>`.
- Evidence accepts URL-only mode through `fileUrl` or `externalVideoLink`, and can attach an uploaded `MediaAsset` while evidence is editable.
- Media upload responses expose `mediaId` and `publicUrl`, not local filesystem paths or storage internals.
- Evidence AI status defaults to `NOT_STARTED`; no AI service is called in the MVP workflow.
- Profile completeness uses linked display name, sport, position, location, organisation or school/club, bio, age, at least one achievement, and at least one evidence item.
- Coach evidence verification requires a CoachProfile and records coach profile, coach organisation, athlete profile, and shared-organisation context where available.
- Organisation names in discovery resolve from `organisationId` first and fall back to `schoolOrClub` text.
- Achievement delete/archive is deferred because the MVP model does not yet include a soft-delete or achievement moderation status.
- Discovery is relational database search for the MVP. Elasticsearch/OpenSearch, caching, vector search, and recommendation ranking are future work.
- LevelPlay Rank uses verified evidence, achievements, coach verification count, and profile completeness only. Popularity, fan votes, views, likes, paid boosts, and AI scoring are not part of the MVP formula.
- Audit logs are append-only through the service/API surface. Delete and edit endpoints are intentionally not provided.
- Notifications are append-only through the service/API surface. Users can mark notifications as read, but delete/edit endpoints and notification preferences are intentionally not provided yet.
