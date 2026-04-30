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
- `/athlete` covers a guided happy path for profile create/update, organisation link, achievements list/create/edit, URL-only evidence, media upload/attach, submit evidence, LevelPlay score/explanation, and inline notifications.
- `/coach` covers a guided happy path for organisation lookup, coach profile create/update, pending evidence review, verification context, verify/reject decisions, and inline notifications.
- `/scout` covers athlete discovery search, athlete discovery profile review, verified evidence search, LevelPlay score/explanation lookup, and inline notifications.
- `/organisation` covers organisation search/detail, verified athlete discovery, verified evidence discovery, and inline notifications.
- `/admin` covers organisation creation/search/update, moderation summary, flagged/archived evidence review, flag/archive/note actions, audit log search, audit logs by target, LevelPlay recalculation for one/all athletes, and inline notifications.
- `/notifications` covers list, unread count, mark one read, and mark all read.

## Usability Polish

The prototype includes a lightweight usability pass for manual testers:

- Role-specific navigation labels and visible current role.
- Guided workflow hints on the dashboard and every role workspace.
- Visible form labels, required markers, and helper text on the main forms.
- Consistent success, loading, empty, and API error messages.
- Reusable status pills for workflow states and LevelPlay tiers.
- Collapsible debug JSON blocks for inspecting backend responses without crowding the main workflow.
- Responsive layout improvements for laptop, tablet-width, and basic mobile browser testing.

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
10. Search/select an organisation if one exists.
11. Create a coach profile.
12. Open pending evidence and verification context.
13. Verify the evidence, or reject a separate submission with a reason.
14. Login as ATHLETE again and check LevelPlay plus notifications.
15. Login as SCOUT_AGENT, search discovery athletes/evidence, open a discovery profile, and inspect LevelPlay explanation.
16. Login as ORGANISATION, search organisations, and review verified discovery results.
17. Login as ADMIN, create/search/update an organisation, inspect moderation summary, flag/archive evidence, add a moderation note, review audit logs, and run LevelPlay recalculation.

## Known Limitations

- The UI is intentionally plain and aimed at backend workflow validation.
- Some responses are still available as collapsible debug blocks so developers can inspect the current API contract.
- Form validation is light; the backend remains the source of truth.
- Media upload has no progress bar, drag-and-drop, thumbnails, transcoding, or playback workflow.
- Screens are now easier for manual testing, but they are still prototype-level rather than product-ready.
- No production auth hardening, push notifications, WebSockets, AI, payments, social features, or deployment automation are included.

## Next Frontend Tasks

- Create a frontend MVP smoke-test checklist and optional lightweight automated frontend tests for auth/dashboard rendering.
- Add deeper field-level validation hints only after the backend contract stabilises further.
- Decide which frontend patterns should graduate from prototype utilities to production components.
- Decide whether the first production client should be React web, React Native, or another mobile framework after MVP validation.
