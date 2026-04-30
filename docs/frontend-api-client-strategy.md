# Frontend API Client Strategy

This document describes how a future frontend or mobile client should consume the Ultron Sport MVP API.

## Base URL Configuration

Use one environment-driven base URL.

Example for a React web prototype:

```ts
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
```

Use primary `/api/...` endpoints for new frontend work. Keep `/api/v1/...` only for legacy compatibility.

## Auth Token Storage

Web prototype:

- Best for early testing: in-memory token storage.
- Acceptable for manual MVP testing: `sessionStorage`.
- Avoid for production-like behavior: `localStorage`, because XSS can expose tokens.

Mobile later:

- Use secure storage through the selected framework.
- React Native should use Keychain/Keystore-backed storage through a vetted library.
- Do not store tokens in plain async storage for production.

## Bearer Token Injection

All protected calls should go through one client wrapper that adds the bearer token.

```ts
const headers: Record<string, string> = {
  "Content-Type": "application/json",
};

if (accessToken) {
  headers.Authorization = `Bearer ${accessToken}`;
}
```

Do not parse JWT internals for UI state. Use login/register response and `GET /api/auth/me`.

## Handling 401 Unauthorised

On `401`:

- Clear the current access token.
- Clear cached current-user state.
- Redirect to login.
- Show a session-expired or login-required message.

Do not retry automatically because the MVP has no refresh token flow.

## Handling 403 Forbidden

On `403`:

- Keep the current session.
- Show a permission message.
- Offer a route back to the role dashboard.

Do not hide all backend errors. A user can still hit `403` through stale UI state, copied URLs, or changed roles.

## Handling Validation Errors

When `code` is `VALIDATION_FAILED`, map `validationErrors` into form state.

```ts
type ApiError = {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  code: string;
  traceId: string;
  validationErrors: Record<string, string>;
};
```

Display rules:

- Field-level messages next to fields.
- General `message` at the form top.
- Optional `traceId` for tester/support reporting.

## Handling Pagination

Use a shared `PageResponse<T>` type.

```ts
type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  sortBy: string;
  sortDirection: "ASC" | "DESC" | string;
};
```

Client rules:

- Default `page=0`.
- Default `size=20`.
- Maximum `size=50`.
- Preserve `sortBy` and `sortDirection` from responses for repeatable pagination controls.

## Handling File And Media Upload

Use `FormData` for media upload.

```ts
const formData = new FormData();
formData.append("file", file);

await fetch(`${baseUrl}/api/media/upload?athleteProfileId=${athleteProfileId}`, {
  method: "POST",
  headers: {
    Authorization: `Bearer ${token}`,
  },
  body: formData,
});
```

Do not set `Content-Type` manually for multipart requests in browsers. Let the runtime add the boundary.

Client-side checks:

- Reject empty files.
- Allow `video/mp4`, `video/quicktime`, `image/jpeg`, `image/png`.
- Reject files above 50MB.

Attach uploaded media to evidence with:

```text
POST /api/evidence/{evidenceId}/media/{mediaId}
```

## Role-Based UI Rendering

The UI should use the current user role to:

- Choose default dashboard.
- Show allowed navigation items.
- Hide unavailable actions.
- Preserve defensive handling for backend `403`.

Suggested guard:

```ts
function canAccess(requiredRoles: UserRole[], currentRole?: UserRole): boolean {
  return Boolean(currentRole && requiredRoles.includes(currentRole));
}
```

## Loading, Error, And Empty States

Every data screen should have:

- Loading state while the request is pending.
- Empty state when `content` is empty or arrays are empty.
- Error state when the request fails.
- Retry action for read-only requests.
- Clear disabled state for submit buttons.

## Suggested TypeScript Types

Use the reference types in:

```text
docs/frontend-types/ultron-sport-api-types.ts
```

These are not production frontend code. Copy or regenerate them into the future frontend project.

## Suggested API Service Modules

```text
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
```

Module responsibilities:

- `authApi`: register, login, me.
- `athleteProfileApi`: create/update/get profile and organisation link.
- `achievementApi`: create/list/update achievements.
- `organisationApi`: create/search/get/update organisations.
- `coachProfileApi`: create/get/update coach profiles.
- `evidenceApi`: create/get/list/update/submit/verify/reject/flag/archive/context.
- `mediaApi`: upload media and get metadata.
- `discoveryApi`: athlete and evidence search.
- `levelPlayApi`: score, explanation, admin recalculation.
- `adminApi`: audit logs, moderation evidence, moderation summary.
- `notificationApi`: list, unread count, mark read.
- `healthApi`: health, readiness, version.

## Minimal API Client Flow

1. Call `POST /api/auth/login`.
2. Store `accessToken` in the chosen MVP token store.
3. Call `GET /api/auth/me`.
4. Route user by `role`.
5. Use role-specific API modules.
6. On `401`, clear token and route to login.
7. On `403`, show permission error.
