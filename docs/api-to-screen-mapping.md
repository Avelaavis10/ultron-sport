# API To Screen Mapping

This document maps backend endpoint groups to the first frontend/mobile prototype screens.

## Shared Response Patterns

Paginated endpoints use:

```ts
PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  sortBy: string;
  sortDirection: string;
}
```

Errors use `ApiError` with `status`, `message`, `code`, `traceId`, and optional `validationErrors`.

## Health

| Endpoint | Screens | Roles | Request DTO | Response DTO |
| --- | --- | --- | --- | --- |
| `GET /api/health` | Landing / Intro, QA smoke check | PUBLIC | None | `HealthResponse` |
| `GET /api/health/readiness` | Landing / Intro, QA smoke check | PUBLIC | None | `HealthReadinessResponse` |
| `GET /api/health/version` | Landing / Intro, About/support | PUBLIC | None | `HealthVersionResponse` |

Loading state: small status indicator.

Empty state: not applicable.

Error state: API unavailable banner.

Frontend validation notes: none.

## Auth

| Endpoint | Screens | Roles | Request DTO | Response DTO |
| --- | --- | --- | --- | --- |
| `POST /api/auth/register` | Register | PUBLIC | `RegisterRequest` | `AuthResponse` |
| `POST /api/auth/login` | Login | PUBLIC | `LoginRequest` | `AuthResponse` |
| `GET /api/auth/me` | Authenticated shell, all dashboards | Authenticated | None | `CurrentUserResponse` |

Loading state: disable submit and show spinner.

Empty state: user is not authenticated.

Error state: show bad credentials, validation, duplicate email, or session-expired message.

Frontend validation notes: email required and valid, password required, registration password 8 to 128 characters, role required.

## Athlete Profiles

| Endpoint | Screens | Roles | Request DTO | Response DTO |
| --- | --- | --- | --- | --- |
| `POST /api/athlete-profiles` | Create athlete profile | ATHLETE | `CreateAthleteProfileRequest` | `AthleteProfileResponse` |
| `GET /api/athlete-profiles/me` | Athlete dashboard, profile edit | ATHLETE | None | `AthleteProfileResponse` |
| `PATCH /api/athlete-profiles/me` | Update athlete profile | ATHLETE | `UpdateAthleteProfileRequest` | `AthleteProfileResponse` |
| `PATCH /api/athlete-profiles/me/organisation` | Link organisation | ATHLETE | `LinkAthleteOrganisationRequest` | `AthleteProfileResponse` |
| `GET /api/athlete-profiles/{id}` | Coach/admin internal profile | ATHLETE owner, COACH, ADMIN | None | `AthleteProfileResponse` |
| `GET /api/athlete-profiles` | Admin athlete profile list | ADMIN | Query params | `PageResponse<AthleteProfileResponse>` |

Loading state: profile skeleton or spinner.

Empty state: no profile yet.

Error state: duplicate profile, ownership failure, validation errors.

Frontend validation notes: sport, position, age, and location required; age 5 to 80; bio max 1000; school/club max 160.

## Achievements

| Endpoint | Screens | Roles | Request DTO | Response DTO |
| --- | --- | --- | --- | --- |
| `POST /api/achievements` | Create achievement | ATHLETE | `CreateAchievementRequest` | `AchievementResponse` |
| `GET /api/achievements/my` | Achievement list | ATHLETE | None | `AchievementResponse[]` |
| `PATCH /api/achievements/{achievementId}` | Update achievement | Owning ATHLETE | `UpdateAchievementRequest` | `AchievementResponse` |
| `GET /api/athlete-profiles/{athleteProfileId}/achievements` | Coach/admin athlete context | ATHLETE owner, COACH, ADMIN | None | `AchievementResponse[]` |
| `GET /api/achievements` | Admin achievement list | ADMIN | Query params | `PageResponse<AchievementResponse>` |

Loading state: achievement list skeleton.

Empty state: no achievements.

Error state: future date, missing title, ownership failure.

Frontend validation notes: title required and max 255; description max 1000; achieved date cannot be future.

## Organisations

| Endpoint | Screens | Roles | Request DTO | Response DTO |
| --- | --- | --- | --- | --- |
| `POST /api/organisations` | Organisation create, admin organisation management | ADMIN, ORGANISATION | `CreateOrganisationRequest` | `OrganisationResponse` |
| `GET /api/organisations` | Organisation search/link, organisation view | Authenticated | Query params | `PageResponse<OrganisationResponse>` |
| `GET /api/organisations/{id}` | Organisation detail | Authenticated | None | `OrganisationResponse` |
| `PATCH /api/organisations/{id}` | Admin organisation update | ADMIN | `UpdateOrganisationRequest` | `OrganisationResponse` |

Loading state: organisation list skeleton.

Empty state: no organisations match search.

Error state: invalid enum, duplicate-like validation, not found, forbidden update.

Frontend validation notes: name, type, location required on create; contact email valid if provided.

## Coach Profiles

| Endpoint | Screens | Roles | Request DTO | Response DTO |
| --- | --- | --- | --- | --- |
| `POST /api/coach-profiles` | Create coach profile | COACH | `CreateCoachProfileRequest` | `CoachProfileResponse` |
| `GET /api/coach-profiles/me` | Coach dashboard/profile | COACH | None | `CoachProfileResponse` |
| `PATCH /api/coach-profiles/me` | Update coach profile | COACH | `UpdateCoachProfileRequest` | `CoachProfileResponse` |
| `GET /api/coach-profiles/{id}` | Coach/admin profile detail | Owning COACH, ADMIN | None | `CoachProfileResponse` |

Loading state: coach profile skeleton.

Empty state: coach profile required before verification.

Error state: duplicate profile, missing certification reference, forbidden access.

Frontend validation notes: certification reference required on create; years experience cannot be negative.

## Evidence

| Endpoint | Screens | Roles | Request DTO | Response DTO |
| --- | --- | --- | --- | --- |
| `POST /api/evidence` | Create evidence | ATHLETE | `CreateEvidenceRequest` | `EvidenceResponse` |
| `GET /api/evidence/{id}` | Evidence detail, verified evidence view | Role-aware | None | `EvidenceResponse` |
| `GET /api/evidence/my` | Athlete evidence list | ATHLETE | None | `EvidenceResponse[]` |
| `PATCH /api/evidence/{id}` | Update evidence | Owning ATHLETE | `UpdateEvidenceRequest` | `EvidenceResponse` |
| `POST /api/evidence/{id}/media/{mediaId}` | Attach media | Owning ATHLETE | None | `EvidenceResponse` |
| `POST /api/evidence/{id}/submit` | Submit evidence | Owning ATHLETE | None | `VerificationActionResponse` |
| `GET /api/evidence/pending-verification` | Coach pending list | COACH, ADMIN | None | `EvidenceResponse[]` |
| `POST /api/evidence/{id}/verify` | Verify evidence | COACH | None | `VerificationActionResponse` |
| `POST /api/evidence/{id}/reject` | Reject evidence | COACH | `RejectEvidenceRequest` | `VerificationActionResponse` |
| `POST /api/evidence/{id}/flag` | Flag evidence | ADMIN | `FlagEvidenceRequest` | `VerificationActionResponse` |
| `POST /api/evidence/{id}/archive` | Archive evidence | ADMIN | None | `VerificationActionResponse` |
| `GET /api/evidence/{id}/verification-context` | Verification context | COACH, ADMIN | None | `VerificationContextResponse` |

Loading state: evidence detail/list skeleton.

Empty state: no evidence or no pending evidence.

Error state: invalid status transition, not visible, missing coach profile, ownership failure.

Frontend validation notes: title, sport, position, event type, match/training, event date required; event date cannot be future; file URL or external video link required.

## Media

| Endpoint | Screens | Roles | Request DTO | Response DTO |
| --- | --- | --- | --- | --- |
| `POST /api/media/upload?athleteProfileId={id}` | Upload media | ATHLETE | Multipart file | `UploadMediaResponse` |
| `GET /api/media/{mediaId}` | Media metadata | Owner ATHLETE, ADMIN | None | `MediaAssetResponse` |

Loading state: upload progress or spinner.

Empty state: no file selected.

Error state: empty file, unsupported content type, file too large, forbidden owner.

Frontend validation notes: accepted types are `video/mp4`, `video/quicktime`, `image/jpeg`, `image/png`; max 50MB.

## Discovery

| Endpoint | Screens | Roles | Request DTO | Response DTO |
| --- | --- | --- | --- | --- |
| `GET /api/discovery/athletes` | Athlete discovery search | Authenticated | Query params | `PageResponse<AthleteDiscoveryCardResponse>` |
| `GET /api/discovery/athletes/{athleteProfileId}` | Discovery profile | Authenticated | None | `AthleteDiscoveryProfileResponse` |
| `GET /api/discovery/evidence` | Evidence discovery search | Authenticated | Query params | `PageResponse<EvidenceDiscoveryCardResponse>` |

Loading state: search results skeleton.

Empty state: no athletes/evidence match filters.

Error state: invalid enum/filter/sort, page size above 50.

Frontend validation notes: clamp page size to 50; use uppercase enum values for statuses and tiers.

## LevelPlay

| Endpoint | Screens | Roles | Request DTO | Response DTO |
| --- | --- | --- | --- | --- |
| `GET /api/levelplay/me` | Athlete LevelPlay | ATHLETE | None | `LevelPlayScoreResponse` |
| `GET /api/levelplay/athletes/{athleteProfileId}` | Discovery profile, score view | Authenticated allowed roles | None | `LevelPlayScoreResponse` |
| `GET /api/levelplay/athletes/{athleteProfileId}/explain` | Score explanation | Authenticated allowed roles | None | `LevelPlayScoreExplanationResponse` |
| `POST /api/levelplay/athletes/{athleteProfileId}/recalculate` | Admin score support | ADMIN | None | `LevelPlayScoreResponse` |
| `POST /api/levelplay/recalculate-all` | Admin score support | ADMIN | None | `LevelPlayScoreResponse[]` |

Loading state: score card skeleton.

Empty state: no score yet.

Error state: not found, forbidden recalculation.

Frontend validation notes: no client scoring. Display backend values exactly.

## Admin Moderation And Audit

| Endpoint | Screens | Roles | Request DTO | Response DTO |
| --- | --- | --- | --- | --- |
| `GET /api/admin/audit-logs` | Audit logs | ADMIN | Query params | `PageResponse<AdminActionLogResponse>` |
| `GET /api/admin/audit-logs/{id}` | Audit detail | ADMIN | None | `AdminActionLogResponse` |
| `GET /api/admin/audit-logs/target/{targetType}/{targetId}` | Target history | ADMIN | Path params | `AdminActionLogResponse[]` |
| `GET /api/admin/moderation/evidence/flagged` | Flagged evidence | ADMIN | None | `EvidenceResponse[]` |
| `GET /api/admin/moderation/evidence/archived` | Archived evidence | ADMIN | None | `EvidenceResponse[]` |
| `POST /api/admin/moderation/evidence/{evidenceId}/note` | Moderation note | ADMIN | `CreateModerationNoteRequest` | `AdminActionLogResponse` |
| `GET /api/admin/moderation/summary` | Admin dashboard | ADMIN | None | `ModerationSummaryResponse` |

Loading state: admin list skeleton.

Empty state: no logs, no flagged evidence, no archived evidence.

Error state: non-admin forbidden, invalid enum/filter, not found.

Frontend validation notes: moderation note `details` required; reason max 1200; details max 2000.

## Notifications

| Endpoint | Screens | Roles | Request DTO | Response DTO |
| --- | --- | --- | --- | --- |
| `GET /api/notifications` | Notifications | Authenticated | Query params | `PageResponse<NotificationResponse>` |
| `GET /api/notifications/unread` | Notifications | Authenticated | Query params | `PageResponse<NotificationResponse>` |
| `GET /api/notifications/unread-count` | Dashboard badges | Authenticated | None | `NotificationUnreadCountResponse` |
| `POST /api/notifications/{notificationId}/read` | Notifications | Owning user | None | `MarkNotificationReadResponse` |
| `POST /api/notifications/read-all` | Notifications | Authenticated | None | `MarkAllNotificationsReadResponse` |

Loading state: notification list skeleton.

Empty state: no notifications.

Error state: invalid status, ownership failure, not found.

Frontend validation notes: status values are `UNREAD` and `READ`; page size max 50.
