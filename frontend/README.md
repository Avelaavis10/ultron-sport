# Ultron Sport React MVP Prototype

This folder contains a minimal React + Vite + TypeScript web prototype for manually validating the Ultron Sport backend API. It is a browser-based MVP workflow tool, not the final production frontend.

## Stack

- Vite
- React
- TypeScript
- React Router
- Native `fetch` through a small shared API client
- Plain CSS

The prototype intentionally avoids Redux, Tailwind, Next.js, server-side rendering, UI component libraries, push notifications, WebSockets, AI, payments, and production deployment concerns.

## Environment

Copy `.env.example` to `.env` if you want local overrides:

```powershell
Copy-Item .env.example .env
```

Default backend URL:

```text
VITE_API_BASE_URL=http://localhost:8080
```

## Run The Backend

From the repository root:

```powershell
mvn spring-boot:run
```

The backend allows `http://localhost:5173` by default for local Vite development through `ULTRON_CORS_ALLOWED_ORIGINS`.

## Install And Run The Frontend

From this `frontend` folder:

```powershell
npm install
npm run dev
```

Open:

```text
http://localhost:5173
```

Build the prototype:

```powershell
npm run build
```

## Implemented Screens

- `/health` checks health, readiness, and version endpoints.
- `/register` creates users for ATHLETE, COACH, ORGANISATION, SCOUT_AGENT, or ADMIN.
- `/login` stores the backend JWT access token for the current browser session.
- `/dashboard` routes users toward the correct role workspace.
- `/athlete` covers profile, organisation link, achievements, URL-only evidence, media upload/attach, submit evidence, LevelPlay, and notifications.
- `/coach` covers coach profile, pending evidence, verify/reject, verification context, and notifications.
- `/scout` covers discovery search, verified evidence search, discovery profile, and LevelPlay explanation.
- `/organisation` covers organisation search and verified discovery.
- `/admin` covers organisation creation, moderation summary, flagged/archived evidence, audit logs, moderation notes, and LevelPlay recalculation.
- `/notifications` covers list, unread count, mark one read, and mark all read.

## Token Handling

For this prototype only, the access token and current user are stored in `sessionStorage`. This is convenient for manual API validation but is not the final production security strategy.

Production mobile/web clients should use framework-appropriate secure storage and a reviewed token lifecycle. Refresh tokens, MFA, OAuth, remember-me, and advanced session security are intentionally out of scope for this MVP scaffold.

## Manual MVP Flow

1. Start the backend.
2. Start the frontend.
3. Register or login as ATHLETE.
4. Create an athlete profile.
5. Create an achievement.
6. Create URL-only evidence.
7. Optionally upload and attach media.
8. Submit evidence.
9. Login as COACH.
10. Create a coach profile.
11. Verify the evidence.
12. Login as ATHLETE again and check LevelPlay plus notifications.
13. Login as SCOUT_AGENT and search discovery.
14. Login as ADMIN and inspect moderation summary plus audit logs.

## Known Limitations

- The UI is intentionally plain and aimed at backend workflow validation.
- Many responses are shown as JSON blocks so developers can inspect the current API contract.
- Form validation is light; the backend remains the source of truth.
- Media upload has no progress bar, drag-and-drop, thumbnails, transcoding, or playback workflow.
- No production auth hardening, push notifications, WebSockets, AI, payments, social features, or deployment automation are included.

## Next Frontend Tasks

- Expand the athlete and coach happy paths into more guided screens.
- Add clearer success states and field-level validation hints.
- Add reusable form components once the API workflow is stable.
- Decide whether the first production client should be React web, React Native, or another mobile framework after MVP validation.
