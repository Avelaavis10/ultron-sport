# Role-Based Navigation Plan

This plan describes how the first frontend prototype should route and hide navigation by role. Backend RBAC remains the real security layer.

## Public / Auth Navigation

Default route:

- Landing / intro screen.

Allowed navigation items:

- Register.
- Login.
- Health status.

Hidden navigation items:

- All authenticated dashboards.
- Evidence, discovery, admin, notifications, and media management.

Backend endpoint groups used:

- Health.
- Auth.

Important restrictions:

- Public access remains minimal.
- No unauthenticated discovery in the MVP.

## ATHLETE

Default dashboard:

- Athlete dashboard.

Allowed navigation items:

- My profile.
- Link organisation.
- My achievements.
- My evidence.
- Create evidence.
- Upload/attach media.
- My LevelPlay.
- Notifications.
- Discovery search for public/verified profiles.

Hidden navigation items:

- Coach verification queue.
- Admin moderation.
- Audit logs.
- Organisation update tools.
- Scout-only dashboard framing.

Backend endpoint groups used:

- Auth.
- Athlete profiles.
- Achievements.
- Evidence.
- Media.
- LevelPlay.
- Discovery.
- Organisations.
- Notifications.

Important restrictions:

- ATHLETE can manage only own profile, achievements, evidence, media, and notifications.
- ATHLETE cannot verify, reject, flag, or archive evidence.
- ATHLETE can update evidence only while DRAFT or REJECTED.

## COACH

Default dashboard:

- Coach dashboard.

Allowed navigation items:

- Coach profile.
- Pending verification.
- Evidence verification detail.
- Verification context.
- Athlete profile context.
- LevelPlay explanation.
- Notifications.
- Discovery search.

Hidden navigation items:

- Athlete evidence creation.
- Media upload.
- Admin audit/moderation.
- Organisation update tools.

Backend endpoint groups used:

- Auth.
- Coach profiles.
- Evidence.
- Athlete profiles.
- Achievements.
- Organisations.
- LevelPlay.
- Discovery.
- Notifications.

Important restrictions:

- COACH must create a CoachProfile before verify/reject actions.
- COACH cannot archive evidence.
- COACH cannot access `/api/admin/**`.

## ORGANISATION

Default dashboard:

- Organisation dashboard.

Allowed navigation items:

- Organisation search/view.
- Create organisation.
- Athlete discovery search.
- Verified evidence view.
- LevelPlay explanation.
- Notifications.

Hidden navigation items:

- Athlete profile editing.
- Coach profile editing.
- Evidence creation.
- Evidence verification.
- Admin moderation and audit.

Backend endpoint groups used:

- Auth.
- Organisations.
- Discovery.
- Evidence visible by status.
- LevelPlay.
- Notifications.

Important restrictions:

- ORGANISATION users see VERIFIED evidence only through discovery and visible evidence endpoints.
- Organisation record update is ADMIN-only for now.

## SCOUT_AGENT

Default dashboard:

- Scout dashboard.

Allowed navigation items:

- Athlete discovery search.
- Athlete discovery profile.
- Verified evidence view.
- LevelPlay score/explanation.
- Notifications.

Hidden navigation items:

- Athlete profile management.
- Achievement management.
- Evidence creation/update/submit.
- Media upload.
- Coach verification.
- Organisation management.
- Admin moderation and audit.

Backend endpoint groups used:

- Auth.
- Discovery.
- Evidence visible by status.
- LevelPlay.
- Notifications.

Important restrictions:

- SCOUT_AGENT users can see VERIFIED evidence only.
- They cannot create, update, verify, reject, flag, archive, or moderate records.

## ADMIN

Default dashboard:

- Admin dashboard.

Allowed navigation items:

- Moderation summary.
- Flagged evidence.
- Archived evidence.
- Audit logs.
- Organisation management.
- Athlete profile list.
- Achievement list.
- Verification context.
- LevelPlay recalculation.
- Media metadata support view.
- Notifications.

Hidden navigation items:

- Athlete-only create evidence flow unless testing as a different role.
- Coach-only profile management unless testing as a different role.

Backend endpoint groups used:

- Auth.
- Admin moderation/audit.
- Organisations.
- Athlete profiles.
- Achievements.
- Evidence.
- LevelPlay.
- Media.
- Notifications.

Important restrictions:

- ADMIN can moderate, audit, recalculate LevelPlay, and inspect verification context.
- Admin routes should always sit under an admin layout and call `/api/admin/**` where applicable.

## Navigation Implementation Notes

- Load current user once after login with `GET /api/auth/me`.
- Keep a role-to-route map in the frontend.
- Hide disallowed navigation items, but keep backend `403` handling.
- Deep links should run the same role guard as menu clicks.
- If role is unknown, route to login or an account-error screen.
