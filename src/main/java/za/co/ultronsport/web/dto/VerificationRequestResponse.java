package za.co.ultronsport.web.dto;

import java.time.Instant;
import za.co.ultronsport.domain.VerificationRequest;
import za.co.ultronsport.domain.VerificationStatus;

public record VerificationRequestResponse(
        Long id,
        Long evidenceUploadId,
        Long requestedByUserId,
        Long verifierUserId,
        VerificationStatus status,
        String comments,
        Instant decidedAt
) {
    public static VerificationRequestResponse from(VerificationRequest request) {
        return new VerificationRequestResponse(request.getId(), request.getEvidenceUploadId(),
                request.getRequestedByUserId(), request.getVerifierUserId(), request.getStatus(),
                request.getComments(), request.getDecidedAt());
    }
}
