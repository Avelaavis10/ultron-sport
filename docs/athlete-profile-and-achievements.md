# Athlete Profiles And Achievements

## Purpose

This MVP slice keeps athlete profile data and achievement data clean enough for discovery and LevelPlay scoring without adding complex verification or external integrations.

## Athlete Profile Workflow

- ATHLETE users create one athlete profile at `POST /api/athlete-profiles`.
- ATHLETE users view their own profile at `GET /api/athlete-profiles/me`.
- ATHLETE users update their own profile at `PATCH /api/athlete-profiles/me`.
- ATHLETE users link their profile to an organisation at `PATCH /api/athlete-profiles/me/organisation`.
- ADMIN users list profiles at `GET /api/athlete-profiles`.
- ADMIN and COACH users can view a specific full profile for internal workflows.
- SCOUT_AGENT and ORGANISATION users should use discovery endpoints for discovery-safe profile data.

Profile create/update requires sport, position, and location. Bio is capped for MVP data quality. Duplicate athlete profiles for the same user are rejected.

Athletes can link `organisationId` when a school, club, academy, or team record exists. The older `schoolOrClub` text remains supported as a fallback so athletes are not blocked when an organisation record has not been created yet. Linking an organisation triggers LevelPlay recalculation.

## Achievement Workflow

- ATHLETE users create achievements for their own athlete profile at `POST /api/achievements`.
- ATHLETE users list their own achievements at `GET /api/achievements/my`.
- ATHLETE users update their own unverified achievements at `PATCH /api/achievements/{achievementId}`.
- ADMIN and COACH users can view profile achievements for internal context through `/api/athlete-profiles/{athleteProfileId}/achievements`.

Achievement verification is intentionally not expanded yet. The existing MVP model keeps a simple `verified` boolean, and verified achievements cannot be edited by athletes.

Achievement delete/archive is deferred because the current model does not yet include a soft-delete or achievement moderation status.

## Profile Completeness

LevelPlay recalculates profile completeness from nine explainable factors:

- Linked user display name
- Sport
- Position
- Location
- Organisation or school/club
- Bio
- Age
- At least one achievement
- At least one evidence item

The score is deterministic and excludes popularity, likes, views, fan votes, paid boosts, and AI scoring.

## LevelPlay Integration

LevelPlay recalculates when:

- An athlete profile is created.
- An athlete profile is updated.
- An achievement is created.
- An achievement is updated.
- Evidence is created or verified.

Future work can add profile visibility controls, achievement status values, achievement moderation, soft archive/delete, roster membership, and stricter organisation approval workflows.
