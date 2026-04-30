# Frontend And Mobile Integration Plan

This plan prepares the current Spring Boot MVP API for frontend and mobile consumption. It is a planning document only. It does not create a frontend app, production mobile app, AI feature, deployment pipeline, or external infrastructure.

## Recommended First Client Approach

Build a simple React web prototype first, then move to React Native mobile once API consumption is stable.

Why this fits the MVP:

- The backend has many role-based workflows that need fast validation: athlete onboarding, evidence submission, coach verification, scout discovery, admin moderation, and notifications.
- A browser prototype is faster to debug against local `localhost:8080`.
- The same TypeScript DTOs, API modules, form validation rules, and route guards can inform a later React Native app.
- React Native is still the likely mobile direction after the API contract has been exercised.

Recommended order:

1. React web prototype for API validation and role flows.
2. React Native mobile app for athlete, coach, scout, and organisation user experiences.
3. Separate admin web portal if admin moderation grows beyond the MVP screens.

## Alternatives And Trade-Offs

### React Native First

Good for proving the mobile-first user experience early.

Trade-offs:

- Slower local API debugging.
- Secure token storage and multipart media upload require mobile-specific decisions.
- Admin workflows are awkward in a purely mobile client.

### Flutter First

Good if the team strongly prefers Dart and a single mobile-first codebase.

Trade-offs:

- The current backend DTOs and manual examples are easier to translate directly into TypeScript.
- A future web admin portal would likely still need a separate stack.

### Admin/Test Web Portal First

Good for manual QA and moderation.

Trade-offs:

- Does not validate the athlete-first experience as well.
- Could bias the product toward internal tools instead of grassroots athlete usability.

## Proposed Future Frontend Folder Structure

```text
frontend/
  src/
    app/
      App.tsx
      routes.tsx
      roleGuards.tsx
    api/
      apiClient.ts
      authApi.ts
      athleteProfileApi.ts
      achievementApi.ts
      organisationApi.ts
      coachProfileApi.ts
      evidenceApi.ts
      mediaApi.ts
      discoveryApi.ts
      levelPlayApi.ts
      adminApi.ts
      notificationApi.ts
      healthApi.ts
    auth/
      AuthProvider.tsx
      tokenStore.ts
      useAuth.ts
    features/
      athlete/
      coach/
      scout/
      organisation/
      admin/
      notifications/
    components/
      forms/
      layout/
      feedback/
      data/
    types/
      ultronSportApi.ts
    validation/
      forms.ts
    tests/
      api/
      flows/
```

## API Consumption Strategy

- Configure one base URL, defaulting to `http://localhost:8080`.
- Keep primary client calls on `/api/...`.
- Treat `/api/v1/...` as legacy compatibility unless a backend task formally deprecates or removes it.
- Use one API client wrapper for JSON requests and one helper for multipart media upload.
- Decode role and user details from `GET /api/auth/me` or login/register response, not from JWT internals.
- Centralise handling for `ApiError` responses.
- Use `PageResponse<T>` consistently for paginated views.

## Auth And Token Strategy

Web prototype:

- Prefer in-memory token storage during early manual testing.
- `sessionStorage` is acceptable for MVP tester convenience, with the XSS risk documented.
- Avoid `localStorage` for production-like behavior unless the team explicitly accepts the risk.

Mobile later:

- Use platform secure storage such as Keychain or Keystore through the chosen framework.
- Do not store tokens in plain async storage.

Client behavior:

- Add `Authorization: Bearer <accessToken>` to protected requests.
- On `401`, clear the current session and return the user to login.
- On `403`, keep the session but show a role/permission message.
- Do not parse or display JWT internals.

## Role-Based Navigation Strategy

Role routing should be driven by the authenticated user's role:

- ATHLETE: profile, achievements, evidence, media attachment, LevelPlay, notifications.
- COACH: coach profile, pending verification, evidence review, verification context, notifications.
- SCOUT_AGENT: discovery search, athlete discovery profile, verified evidence, LevelPlay explanation.
- ORGANISATION: organisation view, discovery search, verified evidence, LevelPlay.
- ADMIN: moderation, audit logs, organisation management, LevelPlay recalculation, verification context.

Hide navigation items the role cannot use. Still handle backend `403` errors because hidden UI is not security.

## Error Handling Strategy

Frontend should support the standard `ApiError` shape:

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed.",
  "path": "/api/evidence",
  "code": "VALIDATION_FAILED",
  "traceId": "...",
  "validationErrors": {
    "title": "must not be blank"
  }
}
```

Display rules:

- Field errors go next to the relevant input.
- `message` can be shown in an alert/banner.
- `traceId` can be shown in a compact support detail area for testers.
- Do not show raw stack traces or token data.

## Form Validation Strategy

- Mirror backend required fields and length limits.
- Validate obvious constraints before submit: email format, required fields, past dates, positive IDs, max upload size, supported media types.
- Keep backend validation as the source of truth.
- When backend returns `validationErrors`, merge them into the form state.

## Media Upload Strategy

MVP media choices:

- Use URL-only evidence first for quick manual testing.
- Add multipart upload only after the athlete evidence flow is stable.

Frontend upload behavior:

- Accept `video/mp4`, `video/quicktime`, `image/jpeg`, and `image/png`.
- Keep file size under 50MB.
- Upload with `multipart/form-data` to `/api/media/upload?athleteProfileId={id}`.
- Attach returned `mediaId` to evidence through `/api/evidence/{evidenceId}/media/{mediaId}`.
- Do not expose local file paths.

Future work such as object storage, CDN, scanning, transcoding, thumbnails, and resumable upload is intentionally deferred.

## Testing Strategy

Start with:

- Manual smoke testing through `docs/http/ultron-sport-mvp.http`.
- Frontend API client unit tests with mocked `fetch` or HTTP client.
- Role route guard tests.
- Form validation tests for required fields and backend error mapping.
- End-to-end browser smoke tests for the MVP happy path once a prototype exists.

Do not add full production mobile automation until the API consumption pattern is stable.

## Risks And Assumptions

- The backend currently returns JWT access tokens without refresh tokens, so frontend sessions expire and require login.
- Some `/api/v1` routes still exist. New frontend work should avoid depending on them.
- H2 local data resets on restart, so manual testers may need to register users again.
- Multipart media upload requires a real local sample file and may be skipped during early UI prototyping.
- Admin screens can be built in the React web prototype even if the later user-facing app is React Native.

## Next Build Step

Create a minimal React web prototype that implements:

1. Login/register and authenticated shell.
2. Role-based dashboard routing.
3. Athlete profile, achievement, and URL-only evidence flow.
4. Coach pending verification and verify/reject flow.
5. Scout discovery flow.
6. Notifications and admin moderation smoke screens.

Do not start AI work until these API consumption flows are stable.
