# Frontend MVP Smoke-Test Checklist

Use this checklist for a quick confidence pass before or after frontend prototype changes. It complements the deeper manual testing checklist and the `.http` backend request collection.

## Public

- [ ] Health page loads.
- [ ] Register page loads.
- [ ] Login page loads.

## Auth

- [ ] Register each role: ATHLETE, COACH, ORGANISATION, SCOUT_AGENT, ADMIN.
- [ ] Login as each role.
- [ ] Logout works.
- [ ] Current role is visible in the top navigation.

## Athlete

- [ ] Athlete dashboard loads.
- [ ] Create/update profile flow is visible.
- [ ] Achievement flow is visible.
- [ ] Evidence flow is visible.
- [ ] LevelPlay section is visible.
- [ ] Notifications section is visible.

## Coach

- [ ] Coach dashboard loads.
- [ ] Coach profile flow is visible.
- [ ] Pending verification flow is visible.
- [ ] Verify/reject actions are visible.
- [ ] Verification context section is visible.

## Scout

- [ ] Scout dashboard loads.
- [ ] Discovery athlete search is visible.
- [ ] Evidence discovery search is visible.
- [ ] LevelPlay lookup is visible.

## Organisation

- [ ] Organisation dashboard loads.
- [ ] Organisation search is visible.
- [ ] Discovery search is visible.

## Admin

- [ ] Admin dashboard loads.
- [ ] Moderation summary is visible.
- [ ] Flagged/archived evidence views are visible.
- [ ] Audit log search is visible.
- [ ] LevelPlay recalculation controls are visible.

## Error Handling

- [ ] Wrong credentials show a useful error.
- [ ] Missing token redirects or blocks protected route.
- [ ] Wrong role redirects to the dashboard or shows a blocked state.
- [ ] Backend validation error displays clearly.

## Build And Test

- [ ] `npm run build` passes from `frontend/`.
- [ ] `npm run test:run` passes from `frontend/`.

## Notes

- Automated frontend smoke tests mock API responses and do not call the real backend.
- This checklist is intentionally short. Use `docs/frontend-manual-testing-checklist.md` for the full role workflow pass.
- Do not add AI, push notifications, WebSockets, payments, social features, or production deployment concerns during smoke-test maintenance.
