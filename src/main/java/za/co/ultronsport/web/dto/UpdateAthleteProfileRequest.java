package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAthleteProfileRequest(
        @NotBlank @Size(max = 80) String sport,
        @NotBlank @Size(max = 80) String position,
        @NotNull @Min(5) @Max(80) Integer age,
        @Size(max = 40) String gender,
        @NotBlank @Size(max = 160) String location,
        @Size(max = 160) String schoolOrClub,
        Long organisationId,
        @Size(max = 1000) String bio
) {
}
