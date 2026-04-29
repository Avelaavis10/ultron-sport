# LevelPlay Rank

## Purpose

LevelPlay Rank is Ultron Sport's credibility and ranking system. Its goal is to help scouts, agents, coaches, and institutions compare athletes using verified sporting evidence instead of popularity alone.

## Ranking Principles

- Verified performance must matter more than views, likes, or follower count.
- Ranking should be explainable to athletes and recruiters.
- Rankings should be separated by sport and relevant categories.
- The formula must be versioned and recalibrated over time.
- Regional, age, gender, and sport-specific bias must be monitored.
- Suspicious activity must be detected and excluded from ranking.

## MVP Inputs

Recommended MVP ranking inputs:

- Verified evidence: approved videos, statistics, certificates, and competition results
- Achievements: athlete achievements already recorded in the platform
- Coach verification count: evidence approvals from approved coach users
- Profile completeness: enough profile context for scouts and organisations to evaluate fit

The MVP formula intentionally excludes popularity, likes, views, fan votes, paid boosts, and AI-generated scoring.

## Current MVP Formula

The current score is deliberately simple and explainable:

- Verified evidence score: 0 evidence = 0, 1 = 20, 2 = 35, 3 to 5 = 50, more than 5 = 60
- Achievement score: 0 achievements = 0, 1 = 10, 2 = 15, 3 or more = 20
- Coach verification score: 0 verifications = 0, 1 = 10, 2 = 15, 3 or more = 20
- Profile completeness contribution: profile completeness scaled to a maximum of 20 points
- Final credibility score: component sum clamped between 0 and 100

Current profile completeness is deterministic and uses nine factors:

- Linked user display name
- Sport
- Position
- Location
- Organisation or school/club
- Bio
- Age
- At least one achievement
- At least one evidence item

## Tiers

Initial tiers make the score easier to understand:

- Bronze: 0 to 24
- Silver: 25 to 49
- Gold: 50 to 74
- Elite: 75 to 100

Tier names and thresholds should be adjusted after pilot data is available.

## Ranking Categories

Rankings should not compare all athletes in one global list. The system should support category-specific leaderboards:

- Sport
- Position or event
- Age group
- Gender where relevant to the sport
- Region
- School, club, or institution
- Competition level

## Explainability

Each athlete profile should show simple rank factors:

- Verified evidence count
- Latest approved evidence
- Coach or institution endorsements
- Current tier
- Recent rank movement
- Missing actions that could improve credibility, such as requesting verification

## Anti-Fraud Controls

- Only approved verifiers can affect ranking through endorsements.
- Unverified evidence cannot affect ranking.
- Duplicate or tampered files should be detected through file hashes and metadata checks.
- Sudden abnormal engagement should be flagged.
- Verification and admin LevelPlay recalculation actions must be audited.
- Administrators must review flagged evidence and suspicious ranking changes.

## Future Algorithm Direction

The platform can evolve toward Elo-style or score-driven rating models once verified competition results and performance outcomes are available. AI-generated metrics should be introduced only when models are tested, explainable, and monitored for fairness.
