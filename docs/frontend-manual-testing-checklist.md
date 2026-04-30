# Frontend Manual Testing Checklist

Use this checklist after a frontend prototype exists. It follows the current backend contract and complements `docs/http/ultron-sport-mvp.http`.

For a shorter pre-demo pass, use `docs/frontend-smoke-test-checklist.md` first.

## Setup

- [ ] Backend is running at `http://localhost:8080`.
- [ ] Health check: `GET /api/health` returns `UP`.
- [ ] Frontend base URL points to the backend.
- [ ] Prototype navigation shows Health, login/register or dashboard, role workspace, and notifications as appropriate.
- [ ] Test users and payloads from `docs/manual-testing-seed-data.md` are ready.

## Auth Flow

- [ ] Register ADMIN.
- [ ] Register ATHLETE.
- [ ] Register COACH.
- [ ] Register ORGANISATION.
- [ ] Register SCOUT_AGENT.
- [ ] Login each role.
- [ ] Confirm each role lands on the correct dashboard.
- [ ] Confirm the top navigation shows the specific role workspace label and current role badge.
- [ ] Refresh page and confirm token/session behavior matches the chosen MVP token store.

## Athlete Flow

- [ ] ATHLETE creates athlete profile.
- [ ] ATHLETE sees visible form labels, required markers, and helper text.
- [ ] ATHLETE updates athlete profile and sees a success message.
- [ ] ATHLETE sees profile completeness.
- [ ] ATHLETE enters organisation ID or school/club text.
- [ ] ATHLETE links profile to organisation or school/club fallback.
- [ ] ATHLETE creates achievement.
- [ ] ATHLETE sees achievement in list.
- [ ] ATHLETE edits an existing achievement.
- [ ] ATHLETE creates URL-only evidence.
- [ ] ATHLETE sees DRAFT evidence in evidence list.
- [ ] ATHLETE uploads supported media if a local file is available.
- [ ] ATHLETE sees returned media ID and public URL.
- [ ] ATHLETE attaches media to editable evidence if upload was tested.
- [ ] ATHLETE submits evidence.
- [ ] ATHLETE sees evidence move to PENDING_VERIFICATION.
- [ ] ATHLETE sees LevelPlay score and explanation panel after profile or achievement changes.
- [ ] ATHLETE sees inline notifications and can mark one or all as read.

## Coach Flow

- [ ] COACH creates coach profile.
- [ ] COACH sees the workflow hint before starting verification.
- [ ] COACH searches organisations.
- [ ] COACH copies organisation into coach profile form.
- [ ] COACH updates coach profile.
- [ ] COACH sees coach dashboard.
- [ ] COACH sees pending evidence list.
- [ ] COACH opens evidence verification detail.
- [ ] COACH sees verification context.
- [ ] COACH verifies evidence.
- [ ] COACH sees pending list refresh after verification.
- [ ] ATHLETE receives verification notification.
- [ ] Alternate path: COACH rejects a separate pending evidence item with a reason.
- [ ] ATHLETE receives rejection notification.
- [ ] COACH sees inline notifications and can mark one or all as read.

## LevelPlay Flow

- [ ] ATHLETE checks own LevelPlay score.
- [ ] Score reflects verified evidence, achievements, coach verification count, and profile completeness.
- [ ] Score explanation states that popularity and AI scoring are not used.
- [ ] Score or tier change creates athlete notification where applicable.

## Scout / Organisation Discovery Flow

- [ ] SCOUT_AGENT searches athletes.
- [ ] SCOUT_AGENT can complete discovery without opening browser dev tools.
- [ ] SCOUT_AGENT sees verified athlete discovery cards.
- [ ] SCOUT_AGENT opens athlete discovery profile.
- [ ] SCOUT_AGENT sees VERIFIED evidence only.
- [ ] SCOUT_AGENT opens LevelPlay explanation.
- [ ] SCOUT_AGENT searches verified evidence by keyword/sport/position.
- [ ] SCOUT_AGENT sees inline notifications and can mark one or all as read.
- [ ] ORGANISATION searches organisations and loads organisation detail.
- [ ] ORGANISATION searches verified athlete discovery cards.
- [ ] ORGANISATION searches verified evidence.
- [ ] ORGANISATION does not see DRAFT or PENDING_VERIFICATION evidence.
- [ ] ORGANISATION sees inline notifications and can mark one or all as read.

## Admin Flow

- [ ] ADMIN creates organisation.
- [ ] ADMIN can understand moderation/audit forms from visible labels and helper text.
- [ ] ADMIN searches organisations.
- [ ] ADMIN updates organisation details or verification status.
- [ ] ADMIN opens moderation summary.
- [ ] ADMIN flags evidence with reason.
- [ ] ATHLETE receives flag notification.
- [ ] ADMIN views flagged evidence.
- [ ] ADMIN creates moderation note.
- [ ] ADMIN archives evidence.
- [ ] ADMIN views archived evidence.
- [ ] ADMIN searches audit logs with action/target/admin filters.
- [ ] ADMIN views audit logs for target `EVIDENCE/{evidenceId}`.
- [ ] ADMIN recalculates LevelPlay for one athlete.
- [ ] ADMIN recalculates all LevelPlay scores.
- [ ] ADMIN sees inline notifications and can mark one or all as read.

## Notifications Flow

- [ ] ATHLETE opens notifications.
- [ ] ATHLETE filters unread notifications.
- [ ] ATHLETE sees unread count.
- [ ] ATHLETE marks one notification read.
- [ ] ATHLETE marks all notifications read.
- [ ] User cannot mark another user's notification read.

## Negative Tests

- [ ] Unauthenticated user calling `/api/auth/me` sees a `401` error.
- [ ] ATHLETE calling `/api/admin/audit-logs` sees a `403` error.
- [ ] Invalid register/login form shows field errors.
- [ ] Evidence with future `eventDate` shows validation error.
- [ ] Evidence without `fileUrl` or `externalVideoLink` shows validation error.
- [ ] ATHLETE trying to verify evidence receives `403`.
- [ ] COACH without CoachProfile cannot verify evidence.
- [ ] SCOUT_AGENT cannot view non-verified evidence.
- [ ] Discovery request with `size=51` shows a bad request.
- [ ] Invalid enum such as `matchOrTraining=SCRIMMAGE` shows a malformed request or validation error.

## Accessibility And Usability Checks

- [ ] Forms keep labels visible.
- [ ] Error messages are readable and close to fields.
- [ ] Buttons show disabled/loading states.
- [ ] Empty states explain the next useful action.
- [ ] Debug JSON blocks are collapsible and do not dominate the main workflow.
- [ ] Workflow hints make the next manual testing step clear.
- [ ] Role-restricted navigation items are hidden.
- [ ] A backend `403` still shows a clear permission message if reached by direct URL.

## Automated Frontend Smoke Tests

- [ ] `npm run test:run` passes from `frontend/`.
- [ ] Public route smoke tests pass.
- [ ] Role dashboard smoke tests pass for all five roles.
- [ ] Notification and API error component smoke tests pass.

## Handover Notes

- Record any backend response that does not match `docs/api-endpoints-draft.md`.
- Record any missing screen state in `docs/mvp-screen-map.md`.
- Do not add AI, payments, push notifications, WebSockets, or social features during this frontend prototype pass.
