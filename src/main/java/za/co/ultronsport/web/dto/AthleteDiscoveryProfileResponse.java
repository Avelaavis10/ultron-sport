package za.co.ultronsport.web.dto;

import java.time.Instant;
import java.util.List;
import za.co.ultronsport.domain.AthleteProfile;

public record AthleteDiscoveryProfileResponse(
        Long athleteProfileId,
        String displayName,
        String sport,
        String position,
        String location,
        String organisationName,
        String bio,
        List<AchievementSummaryResponse> achievements,
        List<EvidenceDiscoveryCardResponse> evidence,
        LevelPlayScoreSummaryResponse levelPlayScore,
        VerificationSummaryResponse verificationSummary,
        Instant lastUpdated
) {
    public static AthleteDiscoveryProfileResponse from(AthleteProfile profile, String displayName,
                                                       List<AchievementSummaryResponse> achievements,
                                                       List<EvidenceDiscoveryCardResponse> evidence,
                                                       LevelPlayScoreSummaryResponse levelPlayScore,
                                                       VerificationSummaryResponse verificationSummary) {
        return new AthleteDiscoveryProfileResponse(profile.getId(), displayName, profile.getSport(),
                profile.getPosition(), profile.getLocation(), profile.getSchoolOrClub(), profile.getBio(),
                achievements, evidence, levelPlayScore, verificationSummary, profile.getUpdatedAt());
    }
}
