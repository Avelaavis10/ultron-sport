package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateVerificationRequest(
        @NotNull @Positive Long evidenceUploadId,
        @NotNull @Positive Long requestedByUserId,
        @NotNull @Positive Long verifierUserId
) {
}
