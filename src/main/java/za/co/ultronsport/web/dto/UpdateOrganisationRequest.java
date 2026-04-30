package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import za.co.ultronsport.domain.VerificationStatus;

public record UpdateOrganisationRequest(
        @Size(max = 160) String name,
        @Size(max = 60) String type,
        @Size(max = 160) String location,
        @Email String contactEmail,
        VerificationStatus verificationStatus
) {
}
