package za.co.ultronsport.web.dto;

import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.VerificationStatus;

public record AthleteProfileResponse(
        Long id,
        Long userId,
        String sport,
        String position,
        Integer age,
        String gender,
        String location,
        String schoolOrClub,
        Integer profileCompletenessScore,
        VerificationStatus verificationStatus
) {
    public static AthleteProfileResponse from(AthleteProfile profile) {
        return new AthleteProfileResponse(profile.getId(), profile.getUserId(), profile.getSport(),
                profile.getPosition(), profile.getAge(), profile.getGender(), profile.getLocation(),
                profile.getSchoolOrClub(), profile.getProfileCompletenessScore(), profile.getVerificationStatus());
    }
}
