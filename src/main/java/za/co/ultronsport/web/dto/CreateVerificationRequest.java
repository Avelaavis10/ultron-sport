package za.co.ultronsport.web.dto;

import jakarta.validation.constraints.NotNull;

public record CreateVerificationRequest(
        @NotNull Long evidenceUploadId,
        @NotNull Long requestedByUserId,
        @NotNull Long verifierUserId
) {
}
