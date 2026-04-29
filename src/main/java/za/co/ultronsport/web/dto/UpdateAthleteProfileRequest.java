package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAthleteProfileRequest(
        @NotBlank String sport,
        @NotBlank String position,
        @NotNull @Min(5) @Max(80) Integer age,
        String gender,
        @NotBlank String location,
        String schoolOrClub,
        Long organisationId,
        @Size(max = 1000) String bio
) {
}
