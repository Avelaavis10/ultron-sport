# MVP Screen Map

This screen map describes the first frontend/mobile prototype surface. It maps user journeys to backend APIs without implementing any UI.

## Public And Auth Screens

### Landing / Intro Screen

Purpose: introduce Ultron Sport and route users to register or login.

Allowed roles: PUBLIC.

API endpoints consumed:

- `GET /api/health`
- `GET /api/health/version`

Main data displayed:

- App availability.
- Basic MVP status.

Main user actions:

- Go to register.
- Go to login.

Error/empty states:

- API unavailable.
- Version check failed.

MVP notes: keep this functional and small. Do not build a marketing site yet.

### Register Screen

Purpose: create an account for one MVP role.

Allowed roles: PUBLIC.

API endpoints consumed:

- `POST /api/auth/register`

Main data displayed:

- Registration form.
- Role selector for ATHLETE, COACH, ORGANISATION, SCOUT_AGENT, ADMIN during testing.

Main user actions:

- Submit registration.
- Store returned token through the auth layer.

Error/empty states:

- Invalid email.
- Password too short.
- Duplicate email.
- Missing role.

MVP notes: production self-service ADMIN registration should be revisited later.

### Login Screen

Purpose: authenticate an existing user.

Allowed roles: PUBLIC.

API endpoints consumed:

- `POST /api/auth/login`
- `GET /api/auth/me`

Main data displayed:

- Email/password form.

Main user actions:

- Login.
- Route to role dashboard.

Error/empty states:

- Invalid credentials.
- Missing email/password.
- API unavailable.

MVP notes: password reset, MFA, OAuth, and refresh tokens are not part of the current backend.

## Athlete Screens

### Athlete Dashboard

Purpose: give the athlete a quick view of profile status, evidence status, LevelPlay, and notifications.

Allowed roles: ATHLETE.

API endpoints consumed:

- `GET /api/auth/me`
- `GET /api/athlete-profiles/me`
- `GET /api/evidence/my`
- `GET /api/levelplay/me`
- `GET /api/notifications/unread-count`

Main data displayed:

- Profile completeness.
- Latest evidence statuses.
- Current LevelPlay tier.
- Unread notification count.

Main user actions:

- Edit profile.
- Add achievement.
- Create evidence.
- View notifications.

Error/empty states:

- No profile yet.
- No evidence yet.
- No LevelPlay score yet.
- Unauthenticated session.

MVP notes: guide users to create profile before evidence.

### Create / Update Athlete Profile

Purpose: collect athlete identity and sporting context.

Allowed roles: ATHLETE.

API endpoints consumed:

- `POST /api/athlete-profiles`
- `GET /api/athlete-profiles/me`
- `PATCH /api/athlete-profiles/me`

Main data displayed:

- Sport, position, age, gender, location, school/club, organisation, bio.
- Profile completeness score.

Main user actions:

- Create profile.
- Save profile changes.

Error/empty states:

- Duplicate profile.
- Required field missing.
- Age outside 5 to 80.

MVP notes: profile updates trigger LevelPlay recalculation.

### Link Organisation

Purpose: connect athlete profile to an organisation record or school/club fallback text.

Allowed roles: ATHLETE.

API endpoints consumed:

- `GET /api/organisations`
- `PATCH /api/athlete-profiles/me/organisation`

Main data displayed:

- Searchable organisation list.
- Current linked organisation or school/club fallback.

Main user actions:

- Search organisation.
- Link organisation.
- Save school/club fallback.

Error/empty states:

- Organisation not found.
- Invalid organisation ID.

MVP notes: organisation linking affects profile completeness.

### Achievement List

Purpose: show athlete achievements.

Allowed roles: ATHLETE.

API endpoints consumed:

- `GET /api/achievements/my`

Main data displayed:

- Achievement title, description, date, verified flag.

Main user actions:

- Create achievement.
- Edit achievement.

Error/empty states:

- No achievements.
- Session expired.

MVP notes: achievement verification is not a full workflow yet.

### Create / Update Achievement

Purpose: manage athlete achievement records.

Allowed roles: ATHLETE.

API endpoints consumed:

- `POST /api/achievements`
- `PATCH /api/achievements/{achievementId}`

Main data displayed:

- Title, description, achieved date.

Main user actions:

- Save achievement.

Error/empty states:

- Missing title.
- Future date.
- Ownership failure.

MVP notes: achievement changes trigger LevelPlay recalculation.

### Evidence List

Purpose: show all evidence owned by the athlete.

Allowed roles: ATHLETE.

API endpoints consumed:

- `GET /api/evidence/my`

Main data displayed:

- Title, sport, position, event date, media/link, verification status, AI analysis status.

Main user actions:

- Create evidence.
- Edit DRAFT or REJECTED evidence.
- Submit editable evidence.
- Attach media to editable evidence.

Error/empty states:

- No evidence.
- Evidence not editable.

MVP notes: show status transitions clearly.

### Create Evidence

Purpose: create URL-only DRAFT evidence.

Allowed roles: ATHLETE.

API endpoints consumed:

- `POST /api/evidence`
- `PATCH /api/evidence/{evidenceId}`

Main data displayed:

- Evidence metadata form.

Main user actions:

- Save DRAFT.
- Update DRAFT or REJECTED evidence.

Error/empty states:

- Missing title/sport/position/event type.
- Missing both `fileUrl` and `externalVideoLink`.
- Future event date.

MVP notes: URL-only mode is the simplest first UI path.

### Upload / Attach Media

Purpose: upload local/mock media and attach it to editable evidence.

Allowed roles: ATHLETE.

API endpoints consumed:

- `POST /api/media/upload?athleteProfileId={id}`
- `GET /api/media/{mediaId}`
- `POST /api/evidence/{evidenceId}/media/{mediaId}`

Main data displayed:

- Upload status.
- Media filename, content type, checksum, public URL.

Main user actions:

- Select file.
- Upload.
- Attach to evidence.

Error/empty states:

- Empty file.
- Unsupported content type.
- File above 50MB.
- Evidence not editable.

MVP notes: no thumbnails, scanning, transcoding, or production object storage yet.

### Submit Evidence

Purpose: move DRAFT or REJECTED evidence into verification.

Allowed roles: ATHLETE.

API endpoints consumed:

- `POST /api/evidence/{evidenceId}/submit`

Main data displayed:

- Evidence summary and status.

Main user actions:

- Submit for verification.

Error/empty states:

- Invalid status transition.
- Evidence not owned by current athlete.

MVP notes: coach-targeted routing is currently basic.

### LevelPlay Score

Purpose: show the athlete's score and explanation.

Allowed roles: ATHLETE.

API endpoints consumed:

- `GET /api/levelplay/me`
- `GET /api/levelplay/athletes/{athleteProfileId}/explain`

Main data displayed:

- Final score, tier, verified evidence count, achievement count, coach verification count, profile completeness.

Main user actions:

- View explanation.
- Improve profile or evidence.

Error/empty states:

- Score not found yet.

MVP notes: no popularity, likes, views, paid boosts, fan votes, or AI scoring.

### Notifications

Purpose: show in-app notifications.

Allowed roles: ATHLETE, COACH, ORGANISATION, SCOUT_AGENT, ADMIN.

API endpoints consumed:

- `GET /api/notifications`
- `GET /api/notifications/unread`
- `GET /api/notifications/unread-count`
- `POST /api/notifications/{notificationId}/read`
- `POST /api/notifications/read-all`

Main data displayed:

- Notification title, message, type, target, status, created date.

Main user actions:

- Mark one notification read.
- Mark all read.

Error/empty states:

- No notifications.
- Ownership failure.

MVP notes: no email, SMS, push, WebSockets, or preferences.

## Coach Screens

### Coach Dashboard

Purpose: show coach profile status and pending verification work.

Allowed roles: COACH.

API endpoints consumed:

- `GET /api/coach-profiles/me`
- `GET /api/evidence/pending-verification`
- `GET /api/notifications/unread-count`

Main data displayed:

- Coach profile.
- Pending evidence list.
- Unread notifications.

Main user actions:

- Create/update coach profile.
- Open evidence verification detail.

Error/empty states:

- Coach profile missing.
- No pending evidence.

MVP notes: coach profile is required before verify/reject actions.

### Create / Update Coach Profile

Purpose: capture coach verification context.

Allowed roles: COACH.

API endpoints consumed:

- `POST /api/coach-profiles`
- `GET /api/coach-profiles/me`
- `PATCH /api/coach-profiles/me`
- `GET /api/organisations`

Main data displayed:

- Certification reference, organisation, sport, qualification summary, years experience.

Main user actions:

- Save coach profile.
- Link organisation.

Error/empty states:

- Duplicate profile.
- Missing certification reference.
- Negative years experience.

MVP notes: future coach verification approval is deferred.

### Pending Verification List

Purpose: show evidence awaiting coach decision.

Allowed roles: COACH.

API endpoints consumed:

- `GET /api/evidence/pending-verification`

Main data displayed:

- Evidence title, athlete profile ID, sport, event date, status.

Main user actions:

- Open detail.

Error/empty states:

- No pending evidence.
- Coach profile missing when trying to decide.

MVP notes: list is not roster-targeted yet.

### Evidence Verification Detail

Purpose: review submitted evidence and context.

Allowed roles: COACH.

API endpoints consumed:

- `GET /api/evidence/{evidenceId}`
- `GET /api/evidence/{evidenceId}/verification-context`
- `GET /api/athlete-profiles/{athleteProfileId}`
- `GET /api/athlete-profiles/{athleteProfileId}/achievements`

Main data displayed:

- Evidence metadata and media/link.
- Athlete profile summary.
- Achievement list.
- Coach/organisation context.

Main user actions:

- Verify evidence.
- Reject evidence.

Error/empty states:

- Evidence not visible.
- Verification context unavailable.

MVP notes: shared organisation context is recorded but not used for scoring weight yet.

### Verify / Reject Evidence

Purpose: make a coach verification decision.

Allowed roles: COACH.

API endpoints consumed:

- `POST /api/evidence/{evidenceId}/verify`
- `POST /api/evidence/{evidenceId}/reject`

Main data displayed:

- Decision confirmation.
- Rejection reason form.

Main user actions:

- Verify.
- Reject with reason.

Error/empty states:

- Missing rejection reason.
- Invalid status transition.
- Coach profile required.

MVP notes: verification recalculates LevelPlay and notifies athlete.

### Verification Context View

Purpose: inspect athlete, coach, and organisation context.

Allowed roles: COACH, ADMIN.

API endpoints consumed:

- `GET /api/evidence/{evidenceId}/verification-context`

Main data displayed:

- Athlete organisation.
- Coach profile and organisation.
- Shared organisation context.
- Latest verification request.
- MVP warning if context is incomplete.

Main user actions:

- Review before decision.

Error/empty states:

- Context not available.
- Evidence not found.

MVP notes: not visible to SCOUT_AGENT or ORGANISATION users.

## Scout / Agent Screens

### Scout Dashboard

Purpose: start discovery and review saved search context later.

Allowed roles: SCOUT_AGENT.

API endpoints consumed:

- `GET /api/discovery/athletes`
- `GET /api/notifications/unread-count`

Main data displayed:

- Search entry point.
- Featured verified athletes if filters are empty.

Main user actions:

- Search athletes.
- Open athlete profile.

Error/empty states:

- No verified athletes match filters.

MVP notes: shortlists are not implemented yet.

### Athlete Discovery Search

Purpose: filter athletes by profile, evidence, and LevelPlay signals.

Allowed roles: SCOUT_AGENT, ORGANISATION, COACH, ADMIN, ATHLETE.

API endpoints consumed:

- `GET /api/discovery/athletes`

Main data displayed:

- Athlete discovery cards.
- Verified evidence count.
- LevelPlay score and tier.

Main user actions:

- Filter by sport, position, location, keyword, tier, score.
- Open profile.

Error/empty states:

- Empty result set.
- Invalid filter or page size.

MVP notes: scouts and organisations see athletes with verified evidence visibility only.

### Athlete Discovery Profile

Purpose: view a discovery-safe athlete profile.

Allowed roles: SCOUT_AGENT, ORGANISATION, COACH, ADMIN, ATHLETE.

API endpoints consumed:

- `GET /api/discovery/athletes/{athleteProfileId}`
- `GET /api/levelplay/athletes/{athleteProfileId}/explain`

Main data displayed:

- Athlete profile summary.
- Achievement summary.
- Verified evidence list.
- LevelPlay summary and explanation.

Main user actions:

- Open verified evidence.
- Review LevelPlay explanation.

Error/empty states:

- Athlete not visible.
- No verified evidence.

MVP notes: do not expose private/internal verification context here.

### Verified Evidence View

Purpose: inspect a verified evidence item.

Allowed roles: SCOUT_AGENT, ORGANISATION, COACH, ADMIN, owning ATHLETE.

API endpoints consumed:

- `GET /api/evidence/{evidenceId}`
- `GET /api/discovery/evidence`

Main data displayed:

- Evidence title, sport, position, event type, event date, media URL, verification status.

Main user actions:

- Open media/link.

Error/empty states:

- Evidence not found or not visible.

MVP notes: scouts and organisations only see VERIFIED evidence.

### LevelPlay Score / Explanation View

Purpose: understand athlete credibility without hidden ranking logic.

Allowed roles: ATHLETE, COACH, ORGANISATION, SCOUT_AGENT, ADMIN.

API endpoints consumed:

- `GET /api/levelplay/athletes/{athleteProfileId}`
- `GET /api/levelplay/athletes/{athleteProfileId}/explain`

Main data displayed:

- Score inputs and final tier.

Main user actions:

- Review transparent scoring explanation.

Error/empty states:

- Score not calculated yet.

MVP notes: no AI scoring or popularity scoring.

## Organisation Screens

### Organisation Dashboard

Purpose: show organisation profile and discovery access.

Allowed roles: ORGANISATION.

API endpoints consumed:

- `GET /api/organisations`
- `GET /api/discovery/athletes`
- `GET /api/notifications/unread-count`

Main data displayed:

- Organisation search or created record.
- Verified athlete discovery entry point.

Main user actions:

- Create organisation.
- Search verified athletes.

Error/empty states:

- No organisation record.
- No verified athletes.

MVP notes: organisation ownership editing is deferred.

### Organisation Profile / View

Purpose: view organisation details.

Allowed roles: Any authenticated user.

API endpoints consumed:

- `GET /api/organisations/{organisationId}`
- `GET /api/organisations`

Main data displayed:

- Name, type, location, contact email, verification status.

Main user actions:

- Search organisations.

Error/empty states:

- Organisation not found.

MVP notes: admin update screen is separate.

## Admin Screens

### Admin Dashboard

Purpose: route admins to moderation, audit, organisation, and LevelPlay support tasks.

Allowed roles: ADMIN.

API endpoints consumed:

- `GET /api/admin/moderation/summary`
- `GET /api/admin/audit-logs`
- `GET /api/notifications/unread-count`

Main data displayed:

- Moderation counts.
- Recent audit logs.
- Unread notifications.

Main user actions:

- Open flagged evidence.
- Open archived evidence.
- Search audit logs.

Error/empty states:

- No moderation activity.
- No audit logs.

MVP notes: no complex dashboard analytics yet.

### Organisation Management

Purpose: create and update organisation records.

Allowed roles: ADMIN.

API endpoints consumed:

- `POST /api/organisations`
- `GET /api/organisations`
- `GET /api/organisations/{organisationId}`
- `PATCH /api/organisations/{organisationId}`

Main data displayed:

- Organisation list and detail.

Main user actions:

- Create organisation.
- Update organisation.
- Change verification status.

Error/empty states:

- Invalid organisation data.
- Organisation not found.

MVP notes: external registry verification is deferred.

### Audit Logs

Purpose: inspect append-only admin actions.

Allowed roles: ADMIN.

API endpoints consumed:

- `GET /api/admin/audit-logs`
- `GET /api/admin/audit-logs/{id}`
- `GET /api/admin/audit-logs/target/{targetType}/{targetId}`

Main data displayed:

- Admin action type, target type, reason, details, created date.

Main user actions:

- Filter by action, target, admin, date.
- Open target history.

Error/empty states:

- No logs.
- Invalid enum filter.

MVP notes: logs are read-only through API.

### Moderation Summary

Purpose: show evidence status counts.

Allowed roles: ADMIN.

API endpoints consumed:

- `GET /api/admin/moderation/summary`

Main data displayed:

- Flagged, archived, pending, verified, rejected counts.

Main user actions:

- Open evidence lists.

Error/empty states:

- Counts all zero.

MVP notes: no assignment queue yet.

### Flagged Evidence

Purpose: inspect flagged evidence.

Allowed roles: ADMIN.

API endpoints consumed:

- `GET /api/admin/moderation/evidence/flagged`
- `POST /api/admin/moderation/evidence/{evidenceId}/note`

Main data displayed:

- Flagged evidence list.
- Moderation note form.

Main user actions:

- Add note.
- Archive evidence.

Error/empty states:

- No flagged evidence.

MVP notes: appeals are deferred.

### Archived Evidence

Purpose: inspect archived evidence.

Allowed roles: ADMIN.

API endpoints consumed:

- `GET /api/admin/moderation/evidence/archived`

Main data displayed:

- Archived evidence list.

Main user actions:

- Review evidence.

Error/empty states:

- No archived evidence.

MVP notes: unarchive workflow is not implemented.

### LevelPlay Recalculation

Purpose: manually recalculate score for support and QA.

Allowed roles: ADMIN.

API endpoints consumed:

- `POST /api/levelplay/athletes/{athleteProfileId}/recalculate`
- `POST /api/levelplay/recalculate-all`
- `GET /api/levelplay/athletes/{athleteProfileId}/explain`

Main data displayed:

- Score before/after if frontend caches previous value.
- Explanation breakdown.

Main user actions:

- Recalculate one score.
- Recalculate all scores.

Error/empty states:

- Athlete not found.
- No profiles to recalculate.

MVP notes: recalculation creates admin audit logs.
