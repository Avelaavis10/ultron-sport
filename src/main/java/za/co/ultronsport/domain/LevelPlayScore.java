package za.co.ultronsport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "levelplay_scores")
public class LevelPlayScore extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long athleteProfileId;

    private Integer verifiedEvidenceCount;
    private Integer coachVerificationCount;
    private Integer achievementCount;
    private Integer profileCompletenessScore;
    private Integer engagementScore;
    private Integer finalCredibilityScore;

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
        this.verifiedEvidenceCount = safe(verifiedEvidenceCount);
        this.coachVerificationCount = safe(coachVerificationCount);
        this.achievementCount = safe(achievementCount);
        this.profileCompletenessScore = safe(profileCompletenessScore);
        this.engagementScore = safe(engagementScore);
        this.finalCredibilityScore = Math.min(100,
                this.profileCompletenessScore
                        + this.verifiedEvidenceCount * 10
                        + this.coachVerificationCount * 8
                        + this.achievementCount * 5
                        + this.engagementScore);
        this.tier = determineTier(this.finalCredibilityScore);
    }

    private int safe(Integer value) {
        return value == null ? 0 : Math.max(value, 0);
    }

    private LevelPlayTier determineTier(int score) {
        if (score >= 85) {
            return LevelPlayTier.ELITE;
        }
        if (score >= 65) {
            return LevelPlayTier.GOLD;
        }
        if (score >= 40) {
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

    public Integer getEngagementScore() {
        return engagementScore;
    }

    public Integer getFinalCredibilityScore() {
        return finalCredibilityScore;
    }

    public LevelPlayTier getTier() {
        return tier;
    }
}
