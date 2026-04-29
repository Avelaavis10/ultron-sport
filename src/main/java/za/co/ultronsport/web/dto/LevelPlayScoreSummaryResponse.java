package za.co.ultronsport.web.dto;

import za.co.ultronsport.domain.LevelPlayScore;
import za.co.ultronsport.domain.LevelPlayTier;

public record LevelPlayScoreSummaryResponse(
        Integer verifiedEvidenceCount,
        Integer coachVerificationCount,
        Integer achievementCount,
        Integer profileCompletenessScore,
        Integer finalCredibilityScore,
        LevelPlayTier tier
) {
    public static LevelPlayScoreSummaryResponse from(LevelPlayScore score) {
        if (score == null) {
            return null;
        }
        return new LevelPlayScoreSummaryResponse(score.getVerifiedEvidenceCount(), score.getCoachVerificationCount(),
                score.getAchievementCount(), score.getProfileCompletenessScore(), score.getFinalCredibilityScore(),
                score.getTier());
    }
}
