package za.co.ultronsport.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import za.co.ultronsport.domain.AiAnalysisStatus;
import za.co.ultronsport.domain.EvidenceContext;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.VerificationStatus;

public record EvidenceDiscoveryCardResponse(
        Long evidenceId,
        Long athleteProfileId,
        String athleteDisplayName,
        String title,
        String sport,
        String position,
        String eventType,
        EvidenceContext matchOrTraining,
        LocalDate eventDate,
        String mediaUrl,
        VerificationStatus verificationStatus,
        AiAnalysisStatus aiAnalysisStatus,
        Instant createdAt,
        Instant updatedAt
) {
    public static EvidenceDiscoveryCardResponse from(EvidenceUpload evidence, String athleteDisplayName) {
        String mediaUrl = evidence.getExternalVideoLink() != null ? evidence.getExternalVideoLink()
                : evidence.getFileUrl();
        return new EvidenceDiscoveryCardResponse(evidence.getId(), evidence.getAthleteProfileId(),
                athleteDisplayName, evidence.getTitle(), evidence.getSport(), evidence.getPosition(),
                evidence.getEventType(), evidence.getMatchOrTraining(), evidence.getEventDate(), mediaUrl,
                evidence.getVerificationStatus(), evidence.getAiAnalysisStatus(), evidence.getCreatedAt(),
                evidence.getUpdatedAt());
    }
}
