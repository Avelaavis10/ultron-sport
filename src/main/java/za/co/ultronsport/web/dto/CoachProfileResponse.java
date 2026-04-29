package za.co.ultronsport.web.dto;

import za.co.ultronsport.domain.CoachProfile;
import za.co.ultronsport.domain.VerificationStatus;

public record CoachProfileResponse(
        Long id,
        Long userId,
        String certificationReference,
        String organisationName,
        String sport,
        VerificationStatus verificationStatus
) {
    public static CoachProfileResponse from(CoachProfile profile) {
        return new CoachProfileResponse(profile.getId(), profile.getUserId(), profile.getCertificationReference(),
                profile.getOrganisationName(), profile.getSport(), profile.getVerificationStatus());
    }
}
