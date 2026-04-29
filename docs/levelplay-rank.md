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
- Endorsements: coach and institution validations from approved verifiers
- Recency: recent verified activity and active profile maintenance
- Performance metadata: sport-specific metrics captured during upload or verification
- Engagement: views or saves may be shown separately and should have little or no direct ranking effect in the MVP

## Recommended Starting Weighting

The exact formula should be tested during pilot use, but the MVP can start with:

- 60 percent verified evidence quality and quantity
- 25 percent approved coach or institution validation
- 10 percent recency and consistency of verified activity
- 5 percent limited engagement or recruiter interest signals

Engagement should never overpower verified evidence.

## Tiers

Initial tiers can be used to make ranking easier to understand:

- Bronze: credible starting profile with some verified evidence
- Silver: consistent verified performance and active profile
- Gold: strong verified evidence and endorsements
- Elite: top verified performance within a sport/category/region

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
- Verification actions must be audited.
- Administrators must review flagged evidence and suspicious ranking changes.

## Future Algorithm Direction

The platform can evolve toward Elo-style or score-driven rating models once verified competition results and performance outcomes are available. AI-generated metrics should be introduced only when models are tested, explainable, and monitored for fairness.
