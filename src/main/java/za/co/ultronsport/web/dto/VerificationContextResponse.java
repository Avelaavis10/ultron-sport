package za.co.ultronsport.web.dto;

import java.time.Instant;
import za.co.ultronsport.domain.AthleteProfile;
import za.co.ultronsport.domain.CoachProfile;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.Organisation;
import za.co.ultronsport.domain.VerificationRequest;
import za.co.ultronsport.domain.VerificationStatus;

public record VerificationContextResponse(
        Long evidenceId,
        VerificationStatus evidenceStatus,
        Long athleteProfileId,
        Long athleteUserId,
        Long athleteOrganisationId,
        String athleteOrganisationName,
        String athleteSchoolOrClub,
        Long coachUserId,
        Long coachProfileId,
        Long coachOrganisationId,
        String coachOrganisationName,
        Boolean sharedOrganisationContext,
        Long latestVerificationRequestId,
        VerificationStatus latestVerificationStatus,
        String latestVerificationComments,
        Instant latestVerificationDecidedAt,
        String mvpWarning
) {
    public static VerificationContextResponse from(EvidenceUpload evidence,
                                                   AthleteProfile athlete,
                                                   Organisation athleteOrganisation,
                                                   VerificationRequest latestVerification,
                                                   CoachProfile coachProfile,
                                                   Organisation coachOrganisation,
                                                   Boolean sharedOrganisationContext,
                                                   String mvpWarning) {
        return new VerificationContextResponse(evidence.getId(), evidence.getVerificationStatus(),
                athlete.getId(), athlete.getUserId(), athlete.getOrganisationId(),
                organisationName(athleteOrganisation), athlete.getSchoolOrClub(),
                latestVerification == null ? null : latestVerification.getVerifierUserId(),
                coachProfile == null ? null : coachProfile.getId(),
                coachProfile == null ? null : coachProfile.getOrganisationId(),
                organisationName(coachOrganisation), sharedOrganisationContext,
                latestVerification == null ? null : latestVerification.getId(),
                latestVerification == null ? null : latestVerification.getStatus(),
                latestVerification == null ? null : latestVerification.getComments(),
                latestVerification == null ? null : latestVerification.getDecidedAt(),
                mvpWarning);
    }

    private static String organisationName(Organisation organisation) {
        return organisation == null ? null : organisation.getName();
    }
}
