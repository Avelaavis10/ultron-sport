# Form And Validation Mapping

This document maps frontend forms to backend DTO validation. Backend validation remains the source of truth.

## Register

Fields:

- `displayName`
- `email`
- `phone`
- `password`
- `role`

Required fields:

- `displayName`
- `email`
- `password`
- `role`

Backend validation:

- `displayName` not blank, max 120.
- `email` not blank, valid email, max 254.
- `phone` max 40.
- `password` not blank, 8 to 128 characters.
- `role` required.

Suggested frontend validation:

- Show role selector for MVP test roles.
- Validate email format before submit.
- Confirm password length.

Common errors:

- `VALIDATION_FAILED`
- `DUPLICATE_RESOURCE`

Notes: production admin self-registration should be revisited later.

## Login

Fields:

- `email`
- `password`

Required fields:

- `email`
- `password`

Backend validation:

- `email` not blank, valid email, max 254.
- `password` not blank, max 128.

Suggested frontend validation:

- Validate email and required password.

Common errors:

- `VALIDATION_FAILED`
- `BAD_CREDENTIALS`

Notes: no password reset or refresh tokens in MVP.

## Create Athlete Profile

Fields:

- `sport`
- `position`
- `age`
- `gender`
- `location`
- `schoolOrClub`
- `organisationId`
- `bio`

Required fields:

- `sport`
- `position`
- `age`
- `location`

Backend validation:

- `sport` not blank, max 80.
- `position` not blank, max 80.
- `age` required, min 5, max 80.
- `gender` max 40.
- `location` not blank, max 160.
- `schoolOrClub` max 160.
- `bio` max 1000.

Suggested frontend validation:

- Use numeric input for age.
- Trim text fields.
- Show profile completeness preview after save.

Common errors:

- Missing required fields.
- Duplicate athlete profile.

Notes: profile creation creates or recalculates LevelPlayScore.

## Update Athlete Profile

Fields: same as create athlete profile.

Required fields: same as create athlete profile.

Backend validation: same as create athlete profile.

Suggested frontend validation:

- Pre-fill current profile.
- Prevent accidental blanking of required fields.

Common errors:

- Ownership failure.
- Required fields missing.

Notes: profile update recalculates LevelPlayScore and may create a notification.

## Link Athlete Organisation

Fields:

- `organisationId`
- `schoolOrClub`

Required fields:

- None, but at least one is useful for the user journey.

Backend validation:

- `organisationId` positive if provided.
- `schoolOrClub` max 160.

Suggested frontend validation:

- Search organisation first.
- Allow school/club fallback text.

Common errors:

- Organisation not found.
- Invalid ID.

Notes: organisation link affects profile completeness.

## Create Achievement

Fields:

- `athleteProfileId`
- `title`
- `description`
- `achievedAt`

Required fields:

- `athleteProfileId`
- `title`

Backend validation:

- `athleteProfileId` positive.
- `title` not blank, max 255.
- `description` max 1000.
- `achievedAt` cannot be future.

Suggested frontend validation:

- Use the current athlete profile ID automatically.
- Date picker should block future dates.

Common errors:

- Ownership failure.
- Future date.

Notes: creation recalculates LevelPlayScore.

## Update Achievement

Fields:

- `title`
- `description`
- `achievedAt`

Required fields:

- `title`

Backend validation:

- `title` not blank, max 255.
- `description` max 1000.
- `achievedAt` cannot be future.

Suggested frontend validation:

- Date picker should block future dates.

Common errors:

- Ownership failure.
- Achievement not found.

Notes: update recalculates LevelPlayScore.

## Create Organisation

Fields:

- `name`
- `type`
- `location`
- `contactEmail`
- `primaryAdminUserId`

Required fields:

- `name`
- `type`
- `location`

Backend validation:

- `name` not blank, max 160.
- `type` not blank, max 60.
- `location` not blank, max 160.
- `contactEmail` valid email if provided.
- `primaryAdminUserId` positive if provided.

Suggested frontend validation:

- Use a controlled type selector for `SCHOOL`, `CLUB`, `ACADEMY`, `UNIVERSITY`, `TEAM`, or `OTHER` where the UI wants consistency.

Common errors:

- Missing name/type/location.
- Forbidden update when not admin.

Notes: backend currently stores type as text.

## Create Coach Profile

Fields:

- `certificationReference`
- `organisationId`
- `organisationName`
- `sport`
- `qualificationSummary`
- `yearsExperience`

Required fields:

- `certificationReference`

Backend validation:

- `certificationReference` not blank, max 120.
- `organisationId` positive if provided.
- `organisationName` max 160.
- `sport` max 80.
- `qualificationSummary` max 1000.
- `yearsExperience` min 0.

Suggested frontend validation:

- Require coach profile before showing verify/reject buttons as active.
- Use numeric input for years experience.

Common errors:

- Duplicate coach profile.
- Missing certification reference.

Notes: coach profile is required before evidence verification.

## Create Evidence

Fields:

- `athleteProfileId`
- `title`
- `description`
- `sport`
- `position`
- `eventType`
- `matchOrTraining`
- `eventDate`
- `fileUrl`
- `externalVideoLink`

Required fields:

- `athleteProfileId`
- `title`
- `sport`
- `position`
- `eventType`
- `matchOrTraining`
- `eventDate`
- At least one of `fileUrl` or `externalVideoLink`

Backend validation:

- IDs positive.
- `title` not blank, max 255.
- `description` max 1200.
- `sport` max 80.
- `position` max 80.
- `eventType` max 120.
- `matchOrTraining` required, `MATCH` or `TRAINING`.
- `eventDate` cannot be future.
- URLs max 500.

Suggested frontend validation:

- Use date picker that blocks future dates.
- Use radio/segmented control for match/training.
- Require one evidence location before save.

Common errors:

- Missing evidence location.
- Future event date.
- Invalid enum.

Notes: evidence starts as DRAFT.

## Upload Media

Fields:

- `athleteProfileId`
- `file`

Required fields:

- `athleteProfileId`
- `file`

Backend validation:

- `athleteProfileId` positive.
- File must not be empty.
- File type must be supported.
- File size must not exceed 50MB.

Suggested frontend validation:

- Restrict file picker accept list.
- Show selected file type and size.
- Disable upload if no file is selected.

Common errors:

- Unsupported content type.
- Empty file.
- File too large.

Notes: upload does not automatically submit evidence.

## Reject Evidence

Fields:

- `reason`

Required fields:

- `reason`

Backend validation:

- `reason` not blank, max 1200.

Suggested frontend validation:

- Textarea with remaining character count.

Common errors:

- Missing reason.
- Invalid status transition.
- Coach profile required.

Notes: rejection notifies athlete.

## Flag Evidence

Fields:

- `reason`

Required fields:

- `reason`

Backend validation:

- `reason` not blank, max 1200.

Suggested frontend validation:

- Admin-only form.
- Textarea with remaining character count.

Common errors:

- Missing reason.
- Non-admin forbidden.

Notes: flagging creates an audit log and notifies athlete.

## Moderation Note

Fields:

- `reason`
- `details`

Required fields:

- `details`

Backend validation:

- `reason` max 1200.
- `details` not blank, max 2000.

Suggested frontend validation:

- Require details.
- Keep reason optional but visible.

Common errors:

- Missing details.
- Non-admin forbidden.

Notes: note creates an append-only audit log and does not change evidence status.
