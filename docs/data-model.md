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

### Institution

- institution_id
- name
- type
- location
- verification_status
- admin_user_id

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
- type
- title
- description
- file_url
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

### Verification

- verification_id
- evidence_id
- verifier_user_id
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
- user_id
- type
- title
- body
- read_status
- created_at

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
- actor_user_id
- action
- entity_type
- entity_id
- before_state
- after_state
- ip_address
- created_at

## Relationships

- A user has one or more role-specific profiles.
- An athlete profile has many evidence records.
- Evidence has zero or more verification records.
- Verified evidence may produce LevelPlay score updates.
- Institutions can own rosters and assign coaches.
- Scouts and agents can create shortlists and offers.
- Notifications belong to users.
- Audit logs reference sensitive actions across the system.

## Evidence Status Values

- draft
- submitted
- pending_verification
- verified
- rejected
- flagged
- archived

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
- The final credibility score is clamped between 0 and 100.
- Tiers map to BRONZE for 0-24, SILVER for 25-49, GOLD for 50-74, and ELITE for 75-100.

## Indexing Recommendations

- Unique indexes on email and phone where provided.
- B-tree indexes on sport, location, age, gender, position_or_event, school_or_club, and tier.
- MVP discovery indexes include athlete profile sport, position, location, organisation_id, updated_at, and evidence sport, position, verification_status, event_date, athlete_profile_id, created_at, and updated_at.
- LevelPlayScore should keep a unique index on athlete_profile_id for the current score record.
- Foreign-key indexes on user_id, athlete_user_id, evidence_id, institution_id, and verifier_user_id.
- Search index for athlete names, bios, achievements, sports, schools, and locations.
- Cache frequently accessed ranking lists and session lookups.

## Data Governance

- Keep personal data minimal and purpose-bound.
- Store large files in object storage, not the relational database.
- Record model versions for AI metrics.
- Preserve audit trails for verification, ranking, moderation, and account changes.
- Support user data export and deletion workflows.
