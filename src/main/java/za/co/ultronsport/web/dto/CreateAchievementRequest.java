package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateAchievementRequest(
        @NotNull @Positive Long athleteProfileId,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 1000) String description,
        @PastOrPresent
        LocalDate achievedAt
) {
}
