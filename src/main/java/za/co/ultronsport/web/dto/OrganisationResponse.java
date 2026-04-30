package za.co.ultronsport.web.dto;

import za.co.ultronsport.domain.Organisation;
import za.co.ultronsport.domain.VerificationStatus;

public record OrganisationResponse(
        Long id,
        String name,
        String type,
        String location,
        String contactEmail,
        Long primaryAdminUserId,
        VerificationStatus verificationStatus
) {
    public static OrganisationResponse from(Organisation organisation) {
        return new OrganisationResponse(organisation.getId(), organisation.getName(), organisation.getType(),
                organisation.getLocation(), organisation.getContactEmail(), organisation.getPrimaryAdminUserId(),
                organisation.getVerificationStatus());
    }
}
