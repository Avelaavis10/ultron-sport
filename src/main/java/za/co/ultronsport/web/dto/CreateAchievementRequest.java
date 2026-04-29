package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateAchievementRequest(
        @NotNull Long athleteProfileId,
        @NotBlank String title,
        String description,
        LocalDate achievedAt
) {
}
