# Coach Organisation Verification Context

## Purpose

This MVP slice makes coach evidence verification traceable to a coach profile and organisation context without introducing external verification integrations or strict roster rules too early.

## Current Rules

- ADMIN and ORGANISATION users can create organisation records.
- Authenticated users can search and view organisations.
- ADMIN users can update organisation details and verification status.
- COACH users can create, view, and update their own coach profile.
- ATHLETE users can link their athlete profile to an existing organisation or keep a `schoolOrClub` fallback.
- COACH users must have a CoachProfile before approving or rejecting evidence.
- Cross-organisation verification is still allowed for MVP, but the relationship is recorded.
- Athlete organisation-link updates and coach profile saves create simple in-app notifications for the affected user.

## Verification Context Stored

When a coach verifies or rejects evidence, the system records:

- evidence ID
- athlete profile ID
- verifier user ID
- coach profile ID
- coach organisation ID when available
- whether coach and athlete share the same organisation when both are linked
- decision status, comments, and decision time

Admins and coaches can inspect this context through:

```text
GET /api/evidence/{evidenceId}/verification-context
```

## Deferred Work

The MVP does not implement automated coach verification, OCR, public school registry lookup, SAFA or federation integrations, legal identity checks, parent/guardian consent, or complex organisation onboarding. Future work can add roster membership, organisation ownership, stricter verifier policies, and organisation-context weighting in LevelPlay only after pilot review.
