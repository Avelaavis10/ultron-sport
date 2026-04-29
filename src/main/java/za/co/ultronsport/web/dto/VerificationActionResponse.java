package za.co.ultronsport.web.dto;

import java.time.Instant;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.VerificationStatus;

public record VerificationActionResponse(
        Long evidenceId,
        VerificationStatus verificationStatus,
        String message,
        Instant changedAt
) {
    public static VerificationActionResponse from(EvidenceUpload evidence, String message) {
        return new VerificationActionResponse(evidence.getId(), evidence.getVerificationStatus(), message,
                evidence.getUpdatedAt());
    }
}
