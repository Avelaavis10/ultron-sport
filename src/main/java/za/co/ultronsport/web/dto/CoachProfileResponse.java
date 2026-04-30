package za.co.ultronsport.web.dto;

import za.co.ultronsport.domain.CoachProfile;
import za.co.ultronsport.domain.VerificationStatus;

public record CoachProfileResponse(
        Long id,
        Long userId,
        String certificationReference,
        Long organisationId,
        String organisationName,
        String sport,
        String qualificationSummary,
        Integer yearsExperience,
        VerificationStatus verificationStatus
) {
    public static CoachProfileResponse from(CoachProfile profile) {
        return new CoachProfileResponse(profile.getId(), profile.getUserId(), profile.getCertificationReference(),
                profile.getOrganisationId(), profile.getOrganisationName(), profile.getSport(),
                profile.getQualificationSummary(), profile.getYearsExperience(), profile.getVerificationStatus());
    }
}
