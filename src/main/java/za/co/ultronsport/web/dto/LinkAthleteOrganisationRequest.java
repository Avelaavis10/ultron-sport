package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LinkAthleteOrganisationRequest(
        @Positive Long organisationId,
        @Size(max = 160) String schoolOrClub
) {
}
