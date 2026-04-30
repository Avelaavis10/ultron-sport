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
- JSON response blocks remain visible where they help inspect the API contract.
- Form validation is intentionally light.
- The athlete and coach paths are guided enough for MVP manual testing but are not final product screens.
- Scout, organisation, and admin screens still need the same depth of guided workflow treatment.
- Media upload supports only the MVP multipart endpoint, without progress, thumbnails, video playback, or transcoding.
- Discovery search uses basic filters only.
- No AI, production storage, push notifications, WebSockets, analytics, maps, or deployment tooling is included.

## Future React Native Path

The recommended sequence remains:

1. Validate backend workflows with this React web prototype.
2. Stabilise API contracts and frontend data types.
3. Expand scout, organisation, and admin happy paths.
4. Add focused frontend tests and reusable form primitives once workflows settle.
5. Start React Native or another mobile client once the API consumption model is proven.

The API modules and DTO reference types in this prototype can inform the future mobile client, but should be reviewed before production use.
