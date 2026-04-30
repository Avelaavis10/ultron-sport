package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateOrganisationRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 60) String type,
        @NotBlank @Size(max = 160) String location,
        @Email String contactEmail,
        @Positive Long primaryAdminUserId
) {
}
