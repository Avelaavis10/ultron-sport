package za.co.ultronsport.web.dto;

import java.time.Instant;
import za.co.ultronsport.domain.LevelPlayScore;
import za.co.ultronsport.domain.LevelPlayTier;

public record LevelPlayScoreExplanationResponse(
        Long athleteProfileId,
        Integer verifiedEvidenceCount,
        Integer verifiedEvidenceCountScore,
        Integer achievementCount,
        Integer achievementScore,
        Integer coachVerificationCount,
        Integer coachVerificationScore,
        Integer profileCompletenessScore,
        Integer profileCompletenessContribution,
        Integer finalCredibilityScore,
        LevelPlayTier tier,
        String explanationText,
        Instant calculatedAt
) {
    public static LevelPlayScoreExplanationResponse from(LevelPlayScore score) {
        String explanation = "LevelPlay MVP score uses verified evidence, achievements, coach verifications, "
                + "and profile completeness only. Popularity, fan votes, views, likes, paid boosts, and AI scoring "
                + "are not used.";
        return new LevelPlayScoreExplanationResponse(score.getAthleteProfileId(), score.getVerifiedEvidenceCount(),
                score.getEvidenceScore(), score.getAchievementCount(), score.getAchievementScore(),
                score.getCoachVerificationCount(), score.getVerificationScore(), score.getProfileCompletenessScore(),
                score.getProfileCompletenessContribution(), score.getFinalCredibilityScore(), score.getTier(),
                explanation, score.getCalculatedAt());
    }
}
