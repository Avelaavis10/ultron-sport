package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAthleteProfileRequest(
        @NotNull Long userId,
        @NotBlank String sport,
        @NotBlank String position,
        @NotNull @Min(5) @Max(80) Integer age,
        String gender,
        String location,
        String schoolOrClub,
        String bio
) {
}
