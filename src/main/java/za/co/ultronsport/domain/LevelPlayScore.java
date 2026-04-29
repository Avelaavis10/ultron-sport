package za.co.ultronsport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "levelplay_scores")
public class LevelPlayScore extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long athleteProfileId;

    private Integer verifiedEvidenceCount;
    private Integer coachVerificationCount;
    private Integer achievementCount;
    private Integer profileCompletenessScore;
    private Integer evidenceScore;
    private Integer achievementScore;
    private Integer verificationScore;
    private Integer profileCompletenessContribution;
    private Integer engagementScore;
    private Integer finalCredibilityScore;
    private Instant calculatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LevelPlayTier tier;

    protected LevelPlayScore() {
    }

    private LevelPlayScore(Long athleteProfileId) {
        this.athleteProfileId = athleteProfileId;
        this.verifiedEvidenceCount = 0;
        this.coachVerificationCount = 0;
        this.achievementCount = 0;
        this.profileCompletenessScore = 0;
        this.evidenceScore = 0;
        this.achievementScore = 0;
        this.verificationScore = 0;
        this.profileCompletenessContribution = 0;
        this.engagementScore = 0;
        this.finalCredibilityScore = 0;
        this.tier = LevelPlayTier.BRONZE;
    }

    public static LevelPlayScore createPlaceholder(Long athleteProfileId) {
        return new LevelPlayScore(athleteProfileId);
    }

    public void updatePlaceholderMetrics(Integer verifiedEvidenceCount, Integer coachVerificationCount,
                                         Integer achievementCount, Integer profileCompletenessScore,
                                         Integer engagementScore) {
        updateMvpScore(verifiedEvidenceCount, coachVerificationCount, achievementCount, profileCompletenessScore);
    }

    public void updateMvpScore(Integer verifiedEvidenceCount, Integer coachVerificationCount,
                               Integer achievementCount, Integer profileCompletenessScore) {
        this.verifiedEvidenceCount = safe(verifiedEvidenceCount);
        this.coachVerificationCount = safe(coachVerificationCount);
        this.achievementCount = safe(achievementCount);
        this.profileCompletenessScore = clamp(safe(profileCompletenessScore), 0, 100);
        this.evidenceScore = scoreVerifiedEvidence(this.verifiedEvidenceCount);
        this.achievementScore = scoreAchievements(this.achievementCount);
        this.verificationScore = scoreCoachVerifications(this.coachVerificationCount);
        this.profileCompletenessContribution = Math.min(20, Math.round(this.profileCompletenessScore * 0.2f));
        this.engagementScore = 0;
        this.finalCredibilityScore = clamp(this.evidenceScore + this.achievementScore + this.verificationScore
                + this.profileCompletenessContribution, 0, 100);
        this.tier = determineTier(this.finalCredibilityScore);
        this.calculatedAt = Instant.now();
    }

    private int safe(Integer value) {
        return value == null ? 0 : Math.max(value, 0);
    }

    private int scoreVerifiedEvidence(int count) {
        if (count <= 0) {
            return 0;
        }
        if (count == 1) {
            return 20;
        }
        if (count == 2) {
            return 35;
        }
        if (count <= 5) {
            return 50;
        }
        return 60;
    }

    private int scoreAchievements(int count) {
        if (count <= 0) {
            return 0;
        }
        if (count == 1) {
            return 10;
        }
        if (count == 2) {
            return 15;
        }
        return 20;
    }

    private int scoreCoachVerifications(int count) {
        if (count <= 0) {
            return 0;
        }
        if (count == 1) {
            return 10;
        }
        if (count == 2) {
            return 15;
        }
        return 20;
    }

    private int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private LevelPlayTier determineTier(int score) {
        if (score >= 75) {
            return LevelPlayTier.ELITE;
        }
        if (score >= 50) {
            return LevelPlayTier.GOLD;
        }
        if (score >= 25) {
            return LevelPlayTier.SILVER;
        }
        return LevelPlayTier.BRONZE;
    }

    public Long getAthleteProfileId() {
        return athleteProfileId;
    }

    public Integer getVerifiedEvidenceCount() {
        return verifiedEvidenceCount;
    }

    public Integer getCoachVerificationCount() {
        return coachVerificationCount;
    }

    public Integer getAchievementCount() {
        return achievementCount;
    }

    public Integer getProfileCompletenessScore() {
        return profileCompletenessScore;
    }

    public Integer getEvidenceScore() {
        return evidenceScore;
    }

    public Integer getAchievementScore() {
        return achievementScore;
    }

    public Integer getVerificationScore() {
        return verificationScore;
    }

    public Integer getProfileCompletenessContribution() {
        return profileCompletenessContribution;
    }

    public Integer getEngagementScore() {
        return engagementScore;
    }

    public Integer getFinalCredibilityScore() {
        return finalCredibilityScore;
    }

    public LevelPlayTier getTier() {
        return tier;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }
}
