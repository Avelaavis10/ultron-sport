package za.co.ultronsport.web.dto;

import za.co.ultronsport.domain.LevelPlayScore;
import za.co.ultronsport.domain.LevelPlayTier;

public record LevelPlayScoreResponse(
        Long id,
        Long athleteProfileId,
        Integer verifiedEvidenceCount,
        Integer coachVerificationCount,
        Integer achievementCount,
        Integer profileCompletenessScore,
        Integer engagementScore,
        Integer finalCredibilityScore,
        LevelPlayTier tier
) {
    public static LevelPlayScoreResponse from(LevelPlayScore score) {
        return new LevelPlayScoreResponse(score.getId(), score.getAthleteProfileId(),
                score.getVerifiedEvidenceCount(), score.getCoachVerificationCount(),
                score.getAchievementCount(), score.getProfileCompletenessScore(), score.getEngagementScore(),
                score.getFinalCredibilityScore(), score.getTier());
    }
}
