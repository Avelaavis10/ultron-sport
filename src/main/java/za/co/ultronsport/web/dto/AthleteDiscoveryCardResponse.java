package za.co.ultronsport.web.dto;

import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.LevelPlayScore;

public record AthleteDiscoveryCardResponse(
        Long athleteProfileId,
        String displayName,
        String sport,
        String position,
        String location,
        String organisationName,
        long verifiedEvidenceCount,
        String latestVerifiedEvidenceTitle,
        Integer levelPlayScore,
        String levelPlayTier,
        Integer profileCompletenessScore
) {
    public static AthleteDiscoveryCardResponse from(AthleteProfile profile, String displayName,
                                                    long verifiedEvidenceCount,
                                                    String latestVerifiedEvidenceTitle,
                                                    LevelPlayScore levelPlayScore) {
        return new AthleteDiscoveryCardResponse(profile.getId(), displayName, profile.getSport(),
                profile.getPosition(), profile.getLocation(), profile.getSchoolOrClub(), verifiedEvidenceCount,
                latestVerifiedEvidenceTitle,
                levelPlayScore == null ? null : levelPlayScore.getFinalCredibilityScore(),
                levelPlayScore == null ? null : levelPlayScore.getTier().name(),
                profile.getProfileCompletenessScore());
    }
}
