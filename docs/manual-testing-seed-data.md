# Manual Testing Seed Data

This guide gives testers a predictable data set for exercising the MVP manually. It does not add automatic database seeding. Use the `.http` collection in `docs/http/ultron-sport-mvp.http` or equivalent curl/Postman requests.

The default local database is H2 in memory, so data is reset when the app restarts unless another database is configured.

## Recommended Test Users

Use unique email addresses if the same local database has already registered these users.

| Role | Display name | Email | Password |
| --- | --- | --- | --- |
| ADMIN | Admin User | `admin@ultronsport.test` | `password123` |
| ATHLETE | Athlete User | `athlete@ultronsport.test` | `password123` |
| COACH | Coach User | `coach@ultronsport.test` | `password123` |
| ORGANISATION | Organisation User | `organisation@ultronsport.test` | `password123` |
| SCOUT_AGENT | Scout User | `scout@ultronsport.test` | `password123` |

## Sample Organisation

```json
{
  "name": "Ultron Football Academy",
  "type": "ACADEMY",
  "location": "Cape Town",
  "contactEmail": "admin@ultronacademy.example",
  "primaryAdminUserId": null
}
```

After creating the organisation, copy the returned `id` into `organisationId` in later requests.

## Sample Athlete Profile

```json
{
  "sport": "Football",
  "position": "Forward",
  "age": 19,
  "gender": "Female",
  "location": "Cape Town",
  "schoolOrClub": "Ultron Football Academy",
  "organisationId": null,
  "bio": "Fast winger with verified match evidence."
}
```

After creating the athlete profile, copy the returned `id` into `athleteProfileId`.

## Sample Coach Profile

```json
{
  "certificationReference": "SAFA-D-12345",
  "organisationId": 1,
  "organisationName": "Ultron Football Academy",
  "sport": "Football",
  "qualificationSummary": "Youth development coach.",
  "yearsExperience": 6
}
```

Coach verification requires this profile before evidence can be approved or rejected.

## Sample Achievement

```json
{
  "athleteProfileId": 1,
  "title": "Regional Top Scorer",
  "description": "Top scorer in the under-19 regional tournament.",
  "achievedAt": "2024-09-14"
}
```

Achievement creation and update recalculate LevelPlayScore.

## Sample Evidence

```json
{
  "athleteProfileId": 1,
  "title": "Two goals against City FC",
  "description": "Match clip with goals and pressing actions.",
  "sport": "Football",
  "position": "Forward",
  "eventType": "League match",
  "matchOrTraining": "MATCH",
  "eventDate": "2024-09-21",
  "fileUrl": null,
  "externalVideoLink": "https://video.example/evidence/two-goals"
}
```

Use URL-only evidence for the fastest manual path. Multipart local media upload can be tested separately when a local sample file is available.

## Recommended Manual Testing Order

1. Run `GET /api/health`.
2. Register all five MVP roles.
3. Login all five roles and save bearer tokens.
4. Create an organisation as ADMIN.
5. Create an athlete profile as ATHLETE.
6. Link the athlete profile to the organisation.
7. Create a coach profile as COACH and link it to the same organisation.
8. Create an achievement as ATHLETE.
9. Create URL-only evidence as ATHLETE.
10. Submit evidence as ATHLETE.
11. Verify evidence as COACH.
12. View verification context as COACH or ADMIN.
13. View LevelPlay score and explanation.
14. Search discovery athletes and evidence as SCOUT_AGENT.
15. View ATHLETE notifications and mark one read.
16. Flag evidence as ADMIN.
17. View admin audit logs and moderation summary.

## Common Mistakes

- Duplicate email: restart the H2-backed app or change the email suffix.
- Missing bearer token: login first, then add `Authorization: Bearer <token>`.
- Wrong role: use the token for the role documented in the endpoint matrix.
- Coach cannot verify evidence: create the coach profile before verification.
- Evidence cannot be verified: submit the evidence first so it becomes `PENDING_VERIFICATION`.
- Scout cannot see evidence: scouts only see `VERIFIED` evidence.
- Media upload fails: use one of `video/mp4`, `video/quicktime`, `image/jpeg`, or `image/png` and keep the file under 50MB.
- Page size fails: use `size=50` or lower.
- Invalid enum: use uppercase enum values such as `MATCH`, `TRAINING`, `VERIFIED`, `UNREAD`, or `GOLD`.
