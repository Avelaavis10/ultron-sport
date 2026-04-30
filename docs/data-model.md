# Data Model

## Overview

The data model should capture users, profiles, institutions, evidence, verification, ranking, search, notifications, messaging, offers, and audit activity. The MVP can use a relational database for core records and object storage for media.

## Core Entities

### User

- user_id
- role
- name
- email
- phone
- password_hash or external_auth_id
- account_status
- created_at
- updated_at

### Profile

- profile_id
- user_id
- linked_user_display_name
- sport
- position_or_event
- age
- gender
- location
- school_or_club
- organisation_id
- bio
- achievements
- privacy_settings
- verification_status
- profile_completeness_score

### Achievement

- achievement_id
- athlete_profile_id
- title
- description
- achieved_at
- verified
- created_at
- updated_at

### Institution

- institution_id
- name
- type
- location
- contact_email
- verification_status
- admin_user_id

Current MVP organisation records use this institution shape for schools, clubs, academies, universities, teams, and other grassroots organisations.

### CoachProfile

- coach_profile_id
- user_id
- organisation_id
- organisation_name fallback
- certification_reference
- qualification_summary
- sport
- years_experience
- verification_status

### Team or Roster

- roster_id
- institution_id
- sport
- age_group
- coach_user_id
- athlete_user_id
- status

### Evidence

- evidence_id
- athlete_user_id
- athlete_profile_id
- uploaded_by_user_id
- type
- title
- description
- media_asset_id
- file_url
- external_video_link
- file_hash
- file_size
- duration
- sport
- competition
- opponent
- location
- captured_at
- uploaded_at
- status
- metrics_id

### MediaAsset

- media_asset_id
- owner_user_id
- athlete_profile_id
- evidence_upload_id
- original_filename
- stored_filename
- content_type
- file_size_bytes
- checksum_sha256
- storage_provider
- storage_key
- public_url
- upload_status
- scan_status
- created_at
- updated_at

### Verification

- verification_id
- evidence_id
- athlete_profile_id
- verifier_user_id
- coach_profile_id
- organisation_id
- shared_organisation_context
- verifier_role
- status
- comments
- created_at
- decided_at

### Metric

- metrics_id
- evidence_id
- model_version
- speed
- accuracy
- technique_score
- sport_specific_values
- generated_at

### LevelPlayScore

- levelplay_score_id
- athlete_profile_id
- verified_evidence_count
- coach_verification_count
- achievement_count
- profile_completeness_score
- evidence_score
- achievement_score
- verification_score
- profile_completeness_contribution
- final_credibility_score
- tier
- calculated_at
- updated_at

### Scout Shortlist

- shortlist_id
- owner_user_id
- athlete_user_id
- status
- private_notes
- created_at

### Notification

- notification_id
- recipient_user_id
- type
- title
- message
- status
- target_type
- target_id
- read_at
- metadata_json
- created_at
- updated_at

### Offer

- offer_id
- sender_user_id
- receiver_user_id
- terms
- status
- signature_status
- created_at
- updated_at

### Audit Log

- audit_id
- admin_user_id
- admin_email
- admin_display_name
- action_type
- target_type
- target_id
- reason
- details
- created_at

## Relationships

- A user has one or more role-specific profiles.
- An athlete profile has many evidence records.
- An athlete profile has many achievement records.
- An athlete profile has many media assets.
- An athlete profile may link to one organisation and may retain school_or_club text as a fallback.
- A coach profile may link to one organisation and stores qualification/context fields for MVP verification trust.
- Evidence can reference one attached media asset for the current MVP upload flow.
- Evidence has zero or more verification records.
- Verification records can store coach profile, organisation, athlete profile, and shared-organisation context for later admin review.
- Verified evidence may produce LevelPlay score updates.
- Institutions can own rosters and assign coaches.
- Scouts and agents can create shortlists and offers.
- Notifications belong to users.
- Notifications can reference evidence, athlete profiles, achievements, LevelPlay scores, organisations, coach profiles, or system events.
- Audit logs reference sensitive actions across the system.

## Admin Action Values

Current MVP action types include:

- evidence_flagged
- evidence_archived
- levelplay_recalculated
- levelplay_recalculate_all
- moderation_note_created
- user_viewed
- user_status_changed

Current target types include:

- evidence
- athlete_profile
- user
- levelplay_score
- system

## Evidence Status Values

- draft
- submitted
- pending_verification
- verified
- rejected
- flagged
- archived

## Media Values

Storage providers:

- local
- mock
- s3_todo
- azure_blob_todo

Upload statuses:

- uploaded
- failed
- link_only

Scan statuses:

- not_scanned
- pending
- passed
- failed
- skipped_for_mvp

## Notification Values

Statuses:

- unread
- read

Types:

- evidence_submitted
- evidence_verified
- evidence_rejected
- evidence_flagged
- evidence_archived
- levelplay_score_changed
- achievement_created
- athlete_profile_updated
- organisation_linked
- coach_profile_updated
- system

Target types:

- evidence
- athlete_profile
- achievement
- levelplay_score
- organisation
- coach_profile
- system

## LevelPlay Tier Values

- bronze
- silver
- gold
- elite

## MVP LevelPlay Formula

The current LevelPlayScore is one current record per athlete profile. It is intentionally transparent and excludes popularity, fan votes, views, likes, paid boosts, and AI-generated ranking.

- Verified evidence contributes up to 60 points.
- Achievements contribute up to 20 points.
- Coach verifications contribute up to 20 points.
- Profile completeness is scaled to a maximum 20 point contribution.
- Profile completeness uses nine factors: linked display name, sport, position, location, organisation or school/club, bio, age, at least one achievement, and at least one evidence item.
- The final credibility score is clamped between 0 and 100.
- Tiers map to BRONZE for 0-24, SILVER for 25-49, GOLD for 50-74, and ELITE for 75-100.

## Indexing Recommendations

- Unique indexes on email and phone where provided.
- B-tree indexes on sport, location, age, gender, position_or_event, school_or_club, and tier.
- MVP discovery indexes include athlete profile sport, position, location, organisation_id, updated_at, and evidence sport, position, verification_status, event_date, athlete_profile_id, created_at, and updated_at.
- MediaAsset MVP indexes include owner_user_id, athlete_profile_id, evidence_upload_id, and created_at.
- Notification MVP indexes include recipient_user_id, recipient_user_id plus status, target_type plus target_id, and created_at.
- LevelPlayScore should keep a unique index on athlete_profile_id for the current score record.
- Foreign-key indexes on user_id, athlete_user_id, evidence_id, institution_id, and verifier_user_id.
- Search index for athlete names, bios, achievements, sports, schools, and locations.
- Cache frequently accessed ranking lists and session lookups.

## Data Governance

- Keep personal data minimal and purpose-bound.
- Store large files in object storage, not the relational database.
- The MVP stores media bytes locally only for development; production storage should move behind the same media storage interface.
- Do not expose local filesystem paths in API responses.
- Record model versions for AI metrics.
- Preserve audit trails for verification, ranking, moderation, and account changes.
- Treat audit logs as append-only records through the application service/API surface.
- API error responses include a support `traceId` but do not persist request payloads, JWT internals, passwords, stack traces, or filesystem paths.
- Support user data export and deletion workflows.
