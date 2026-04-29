package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCoachProfileRequest(
        @NotNull Long userId,
        @NotBlank String certificationReference,
        String organisationName,
        String sport
) {
}
