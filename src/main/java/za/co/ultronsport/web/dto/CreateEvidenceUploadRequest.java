package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import za.co.ultronsport.domain.EvidenceContext;
import za.co.ultronsport.domain.EvidenceType;

public record CreateEvidenceUploadRequest(
        @NotNull Long uploadedByUserId,
        @NotNull Long athleteProfileId,
        @NotNull EvidenceType evidenceType,
        @NotBlank String sport,
        @NotBlank String position,
        @NotBlank String eventType,
        @NotNull LocalDate uploadDate,
        @NotNull EvidenceContext evidenceContext,
        String fileUrl,
        String externalLink,
        String metadataNotes
) {
    @AssertTrue(message = "Either fileUrl or externalLink must be provided")
    public boolean hasEvidenceLocation() {
        return hasText(fileUrl) || hasText(externalLink);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
