package za.co.ultronsport.web.dto;

import java.time.Instant;
import za.co.ultronsport.domain.LevelPlayScore;
import za.co.ultronsport.domain.LevelPlayTier;

public record LevelPlayScoreResponse(
        Long id,
        Long athleteProfileId,
        Integer verifiedEvidenceCount,
        Integer coachVerificationCount,
        Integer achievementCount,
        Integer profileCompletenessScore,
        Integer evidenceScore,
        Integer achievementScore,
        Integer verificationScore,
        Integer profileCompletenessContribution,
        Integer engagementScore,
        Integer finalCredibilityScore,
        LevelPlayTier tier,
        Instant calculatedAt
) {
    public static LevelPlayScoreResponse from(LevelPlayScore score) {
        return new LevelPlayScoreResponse(score.getId(), score.getAthleteProfileId(),
                score.getVerifiedEvidenceCount(), score.getCoachVerificationCount(),
                score.getAchievementCount(), score.getProfileCompletenessScore(), score.getEvidenceScore(),
                score.getAchievementScore(), score.getVerificationScore(), score.getProfileCompletenessContribution(),
                score.getEngagementScore(), score.getFinalCredibilityScore(), score.getTier(),
                score.getCalculatedAt());
    }
}
