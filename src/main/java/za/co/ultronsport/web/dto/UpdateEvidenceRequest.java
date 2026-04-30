package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import za.co.ultronsport.domain.EvidenceContext;

public record UpdateEvidenceRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 1200) String description,
        @NotBlank @Size(max = 80) String sport,
        @NotBlank @Size(max = 80) String position,
        @NotBlank @Size(max = 120) String eventType,
        @NotNull EvidenceContext matchOrTraining,
        @NotNull @PastOrPresent LocalDate eventDate,
        @Size(max = 500) String fileUrl,
        @Size(max = 500) String externalVideoLink
) {
    @AssertTrue(message = "Either fileUrl or externalVideoLink must be provided")
    public boolean hasEvidenceLocation() {
        return hasText(fileUrl) || hasText(externalVideoLink);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
