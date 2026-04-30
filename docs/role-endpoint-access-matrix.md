# Role Endpoint Access Matrix

This matrix documents what each role can do in the MVP API. It is a handover aid, not a replacement for `SecurityConfig` and service-level ownership checks.

## PUBLIC Endpoints

Public access remains minimal.

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/auth/register` | Register a user |
| POST | `/api/auth/login` | Login and receive a bearer token |
| GET | `/api/health` | Application health |
| GET | `/api/health/readiness` | MVP readiness |
| GET | `/api/health/version` | App version metadata |

## Authenticated Common Endpoints

All authenticated roles can use:

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/auth/me` | Get current user |
| GET | `/api/organisations` | Search organisations |
| GET | `/api/organisations/{organisationId}` | Get organisation |
| GET | `/api/discovery/athletes` | Search discovery-safe athlete cards |
| GET | `/api/discovery/athletes/{athleteProfileId}` | View discovery-safe athlete profile |
| GET | `/api/discovery/evidence` | Search visible evidence cards |
| GET | `/api/notifications` | List own notifications |
| GET | `/api/notifications/unread` | List own unread notifications |
| GET | `/api/notifications/unread-count` | Count own unread notifications |
| POST | `/api/notifications/{notificationId}/read` | Mark owned notification read |
| POST | `/api/notifications/read-all` | Mark all owned notifications read |

## ATHLETE

ATHLETE users manage their own profile, achievements, evidence, media, LevelPlay view, and notifications.

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/athlete-profiles` | Create own athlete profile |
| GET | `/api/athlete-profiles/me` | View own full profile |
| PATCH | `/api/athlete-profiles/me` | Update own profile |
| PATCH | `/api/athlete-profiles/me/organisation` | Link own profile to organisation or school/club text |
| GET | `/api/athlete-profiles/{athleteProfileId}` | View own internal profile |
| GET | `/api/athlete-profiles/{athleteProfileId}/achievements` | View own achievements |
| POST | `/api/achievements` | Create own achievement |
| GET | `/api/achievements/my` | List own achievements |
| PATCH | `/api/achievements/{achievementId}` | Update own achievement |
| POST | `/api/evidence` | Create own DRAFT evidence |
| GET | `/api/evidence/my` | List own evidence |
| GET | `/api/evidence/{evidenceId}` | View own evidence |
| PATCH | `/api/evidence/{evidenceId}` | Update own DRAFT or REJECTED evidence |
| POST | `/api/evidence/{evidenceId}/media/{mediaId}` | Attach own media to own editable evidence |
| POST | `/api/evidence/{evidenceId}/submit` | Submit own evidence for verification |
| POST | `/api/media/upload` | Upload media for own athlete profile |
| GET | `/api/media/{mediaId}` | View own media metadata |
| GET | `/api/levelplay/me` | View own score |
| GET | `/api/levelplay/athletes/{athleteProfileId}` | View visible athlete score |
| GET | `/api/levelplay/athletes/{athleteProfileId}/explain` | View visible score explanation |

Notes:

- ATHLETE users cannot verify, reject, flag, archive, or moderate evidence.
- ATHLETE users cannot update another athlete's profile, achievement, media, or notification.

## COACH

COACH users manage their own coach profile and verify evidence after creating that profile.

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/coach-profiles` | Create own coach profile |
| GET | `/api/coach-profiles/me` | View own coach profile |
| PATCH | `/api/coach-profiles/me` | Update own coach profile |
| GET | `/api/coach-profiles/{coachProfileId}` | View own coach profile or admin-visible profile |
| GET | `/api/evidence/pending-verification` | View pending evidence |
| GET | `/api/evidence/{evidenceId}` | View evidence allowed for verification context |
| POST | `/api/evidence/{evidenceId}/verify` | Verify pending evidence |
| POST | `/api/evidence/{evidenceId}/reject` | Reject pending evidence with reason |
| GET | `/api/evidence/{evidenceId}/verification-context` | View athlete/coach/organisation verification context |
| GET | `/api/athlete-profiles/{athleteProfileId}` | View relevant internal athlete profile |
| GET | `/api/athlete-profiles/{athleteProfileId}/achievements` | View achievements for verification context |
| GET | `/api/levelplay/athletes/{athleteProfileId}` | View athlete LevelPlay score |
| GET | `/api/levelplay/athletes/{athleteProfileId}/explain` | View score explanation |

Notes:

- COACH must have a CoachProfile before verifying or rejecting evidence.
- COACH verification records coach profile, coach organisation, athlete profile, and shared-organisation context where available.
- COACH cannot archive evidence or access admin audit logs.

## ORGANISATION

ORGANISATION users can create organisation records and use discovery-safe search.

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/organisations` | Create organisation |
| GET | `/api/organisations` | Search organisations |
| GET | `/api/organisations/{organisationId}` | View organisation |
| GET | `/api/discovery/athletes` | Search athletes with VERIFIED evidence visibility |
| GET | `/api/discovery/athletes/{athleteProfileId}` | View discovery-safe athlete profile |
| GET | `/api/discovery/evidence` | Search VERIFIED evidence only |
| GET | `/api/levelplay/athletes/{athleteProfileId}` | View athlete LevelPlay score |
| GET | `/api/levelplay/athletes/{athleteProfileId}/explain` | View score explanation |

Notes:

- ORGANISATION users see VERIFIED evidence only through discovery.
- Organisation owner update is deferred; PATCH `/api/organisations/{id}` is ADMIN-only for now.

## SCOUT_AGENT

SCOUT_AGENT users discover athletes and evidence that is safe to expose.

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/discovery/athletes` | Search discovery-safe athlete cards |
| GET | `/api/discovery/athletes/{athleteProfileId}` | View discovery-safe athlete profile |
| GET | `/api/discovery/evidence` | Search VERIFIED evidence only |
| GET | `/api/evidence/{evidenceId}` | View evidence only when visible and VERIFIED |
| GET | `/api/levelplay/athletes/{athleteProfileId}` | View athlete LevelPlay score |
| GET | `/api/levelplay/athletes/{athleteProfileId}/explain` | View score explanation |

Notes:

- SCOUT_AGENT users can see VERIFIED evidence only.
- SCOUT_AGENT users cannot create, update, submit, verify, reject, flag, archive, upload media, or moderate content.

## ADMIN

ADMIN users manage moderation, audit, organisation updates, LevelPlay recalculation, and support views.

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/admin/audit-logs` | Search audit logs |
| GET | `/api/admin/audit-logs/{id}` | View audit log |
| GET | `/api/admin/audit-logs/target/{targetType}/{targetId}` | View target audit history |
| GET | `/api/admin/moderation/evidence/flagged` | View flagged evidence |
| GET | `/api/admin/moderation/evidence/archived` | View archived evidence |
| POST | `/api/admin/moderation/evidence/{evidenceId}/note` | Add moderation note |
| GET | `/api/admin/moderation/summary` | View moderation counts |
| PATCH | `/api/organisations/{organisationId}` | Update organisation |
| GET | `/api/athlete-profiles` | List athlete profiles |
| GET | `/api/achievements` | List achievements |
| POST | `/api/evidence/{evidenceId}/flag` | Flag evidence |
| POST | `/api/evidence/{evidenceId}/archive` | Archive evidence |
| GET | `/api/evidence/{evidenceId}/verification-history` | View verification history |
| GET | `/api/evidence/{evidenceId}/verification-context` | View verification context |
| POST | `/api/levelplay/athletes/{athleteProfileId}/recalculate` | Recalculate one LevelPlay score |
| POST | `/api/levelplay/recalculate-all` | Recalculate all LevelPlay scores |
| GET | `/api/media/{mediaId}` | View media metadata for support |

Notes:

- ADMIN can moderate, audit, recalculate LevelPlay, and inspect verification context.
- ADMIN actions that affect moderation or LevelPlay create append-only audit logs where implemented.
- ADMIN still receives standard 401/403 JSON when unauthenticated or using the wrong role.

## Legacy Compatibility Routes

The project still exposes selected `/api/v1/...` endpoints from earlier slices. They are kept for compatibility and tests, not as the preferred client contract.

Examples:

- `/api/v1/athlete-profiles`
- `/api/v1/achievements`
- `/api/v1/coach-profiles`
- `/api/v1/organisations`
- `/api/v1/verification-requests`
- `/api/v1/levelplay-scores`
- `/api/v1/users`

Future API contract cleanup can either deprecate these formally or route clients entirely through the current `/api/...` surface.
