package za.co.ultronsport.web.dto;

import java.time.Instant;
import za.co.ultronsport.domain.VerificationRequest;
import za.co.ultronsport.domain.VerificationStatus;

public record VerificationRequestResponse(
        Long id,
        Long evidenceUploadId,
        Long athleteProfileId,
        Long requestedByUserId,
        Long verifierUserId,
        Long coachProfileId,
        Long organisationId,
        Boolean sharedOrganisationContext,
        VerificationStatus status,
        String comments,
        Instant decidedAt
) {
    public static VerificationRequestResponse from(VerificationRequest request) {
        return new VerificationRequestResponse(request.getId(), request.getEvidenceUploadId(),
                request.getAthleteProfileId(), request.getRequestedByUserId(), request.getVerifierUserId(),
                request.getCoachProfileId(), request.getOrganisationId(), request.getSharedOrganisationContext(),
                request.getStatus(), request.getComments(), request.getDecidedAt());
    }
}
