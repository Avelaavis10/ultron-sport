package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrganisationRequest(
        @NotBlank String name,
        @NotBlank String type,
        String location,
        Long primaryAdminUserId
) {
}
