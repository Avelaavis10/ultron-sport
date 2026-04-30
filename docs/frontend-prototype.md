# Frontend Prototype

## Purpose

The React web prototype is a lightweight browser tool for validating the Ultron Sport MVP backend API. It lets developers and testers exercise the main backend workflows without building the final mobile app yet.

This is not a production frontend. It intentionally avoids AI, React Native, Flutter, production deployment, push notifications, WebSockets, payments, social feeds, analytics SDKs, Redux, Tailwind, Next.js, and UI component libraries.

## Location

```text
frontend/
```

## Implemented Screens

- Health: `/health`
- Register: `/register`
- Login: `/login`
- Role dashboard: `/dashboard`
- Athlete workspace: `/athlete`
- Coach workspace: `/coach`
- Scout workspace: `/scout`
- Organisation workspace: `/organisation`
- Admin workspace: `/admin`
- Notifications: `/notifications`

## Usability Polish

The prototype now includes a focused manual-testing usability pass:

1. Navigation names the current role workspace instead of using a generic label.
2. Role, session, active route, and logout controls are easier to identify.
3. Each role workspace includes a workflow hint with the recommended test sequence.
4. Main forms use visible labels, helper text, and required markers.
5. Success, loading, empty, and backend error states are more consistent.
6. Statuses such as `VERIFIED`, `PENDING_VERIFICATION`, `FLAGGED`, `READ`, `UNREAD`, and LevelPlay tiers use a shared status pill.
7. Raw backend responses are still available through collapsible debug blocks, but the main screen prioritises readable cards and summaries.
8. CSS now handles tablet-width and basic mobile-width browser testing more gracefully.

## Automated Smoke Tests

The frontend now has a lightweight smoke-test setup using Vitest, React Testing Library, `@testing-library/jest-dom`, and jsdom.

Run from `frontend/`:

```powershell
npm run test:run
```

The smoke tests cover:

- Public health, login, and register routes.
- Authenticated dashboard shell.
- ATHLETE, COACH, SCOUT_AGENT, ORGANISATION, and ADMIN workspace rendering.
- Protected-route and wrong-role redirect behaviour.
- Standard API error rendering.
- Notification section rendering with mocked API data.

The tests use mocked API responses. They do not call the real backend and do not replace manual full-flow testing.

## Athlete Happy Path

The athlete workspace now supports the main MVP validation path:

1. Load, create, and update the current athlete profile.
2. Save organisation or school/club linking information.
3. List, create, and edit achievements.
4. List evidence and create URL-only evidence.
5. Upload supported media through the MVP multipart endpoint.
6. Attach uploaded media to DRAFT or REJECTED evidence.
7. Submit editable evidence for verification.
8. View LevelPlay score and explanation.
9. View notifications, refresh unread count, and mark notifications read.

Actions remain guarded by the backend. The frontend disables obvious unavailable actions, but backend validation and RBAC remain authoritative.

## Coach Happy Path

The coach workspace now supports the main verification path:

1. Load, create, and update the current coach profile.
2. Search organisations and copy an organisation into the coach profile form.
3. View pending verification evidence.
4. Open verification context for a selected evidence item.
5. Verify pending evidence.
6. Reject pending evidence with a reason.
7. View notifications and mark notifications read.

The UI shows the coach profile prerequisite before verification. The backend still enforces that requirement.

## Scout Happy Path

The scout workspace now supports discovery review for verified athlete data:

1. Search athlete discovery cards by keyword, sport, position, location, page, and size.
2. Open an athlete discovery profile from a search result or by athlete profile ID.
3. Review discovery-safe profile details, verified evidence, achievements, and LevelPlay summary.
4. Search verified evidence by keyword, sport, position, page, and size.
5. Look up LevelPlay score and explanation for an athlete profile.
6. View scout notifications and mark notifications read.

Backend discovery rules still hide draft and pending evidence from SCOUT_AGENT users.

## Organisation Happy Path

The organisation workspace now supports organisation-side discovery validation:

1. Search organisations by name, type, location, verification status, page, and size.
2. Load organisation details by ID.
3. Search verified athlete discovery cards.
4. Search verified evidence.
5. View organisation notifications and mark notifications read.

Organisation users consume discovery-safe data only. Organisation ownership and roster workflows remain future work.

## Admin Happy Path

The admin workspace now supports the main moderation and audit validation path:

1. Create organisations.
2. Search and update organisations.
3. View moderation summary counts.
4. Flag evidence, archive evidence, and add moderation notes.
5. View flagged and archived evidence lists.
6. Search audit logs with action, target, admin, pagination, and sort filters.
7. Load audit logs for a specific target.
8. Recalculate LevelPlay for one athlete profile or all athlete profiles.
9. View admin notifications and mark notifications read.

The admin workspace intentionally remains a prototype surface, not a production moderation console.

## API Modules

The frontend uses one shared API client plus small endpoint modules:

- `apiClient`
- `authApi`
- `healthApi`
- `athleteProfileApi`
- `achievementApi`
- `organisationApi`
- `coachProfileApi`
- `evidenceApi`
- `mediaApi`
- `discoveryApi`
- `levelPlayApi`
- `notificationApi`
- `adminApi`

The shared client reads `VITE_API_BASE_URL`, injects `Authorization: Bearer <token>` when available, supports JSON and multipart requests, passes query parameters, parses the backend `ApiError` response, and clears the local session on 401 responses.

## Role-Based Navigation

Public users can access health, login, and register screens.

Authenticated users can access `/dashboard` and `/notifications`.

Role workspaces are guarded client-side:

- ATHLETE: `/athlete`
- COACH: `/coach`
- ORGANISATION: `/organisation`
- SCOUT_AGENT: `/scout`
- ADMIN: `/admin`

Backend RBAC remains the source of truth. The frontend guards are for navigation clarity only.

## Token Handling

The prototype stores the JWT access token and current user in `sessionStorage` for MVP testing. This is intentionally simple and should not be treated as the production web or mobile token strategy.

Future production clients should use reviewed secure storage and token lifecycle rules. Refresh tokens, MFA, OAuth, device trust, and remember-me behaviour are intentionally deferred.

## Backend CORS

The backend now has a minimal configurable local development CORS setting:

```text
ULTRON_CORS_ALLOWED_ORIGINS=http://localhost:5173
```

This exists only to let the Vite dev server call the Spring Boot API locally. It does not change JWT or RBAC behaviour.

## Known Limitations

- Screens are workflow validation surfaces rather than polished product UI.
- Collapsible JSON response blocks remain where they help inspect the API contract.
- Form validation is intentionally light; backend validation remains authoritative.
- Role workspaces are guided enough for MVP manual testing but are not final product screens.
- Detail views and field-level guidance can still be deepened once testers give feedback.
- Media upload supports only the MVP multipart endpoint, without progress, thumbnails, video playback, or transcoding.
- Discovery search uses basic filters only.
- No AI, production storage, push notifications, WebSockets, analytics, maps, or deployment tooling is included.

## Future React Native Path

The recommended sequence remains:

1. Validate backend workflows with this React web prototype.
2. Stabilise API contracts and frontend data types.
3. Refine reusable form primitives only where tester feedback shows repeated friction.
4. Add deeper frontend validation tests once the UI contract stabilises.
5. Start React Native or another mobile client once the API consumption model is proven.

The API modules and DTO reference types in this prototype can inform the future mobile client, but should be reviewed before production use.
