package za.co.ultronsport.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import za.co.ultronsport.domain.AiAnalysisStatus;
import za.co.ultronsport.domain.EvidenceContext;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.VerificationStatus;

public record EvidenceResponse(
        Long id,
        Long athleteProfileId,
        Long uploadedByUserId,
        String title,
        String description,
        String sport,
        String position,
        String eventType,
        EvidenceContext matchOrTraining,
        LocalDate eventDate,
        String fileUrl,
        String externalVideoLink,
        Long mediaAssetId,
        VerificationStatus verificationStatus,
        AiAnalysisStatus aiAnalysisStatus,
        Instant createdAt,
        Instant updatedAt
) {
    public static EvidenceResponse from(EvidenceUpload evidence) {
        return new EvidenceResponse(evidence.getId(), evidence.getAthleteProfileId(), evidence.getUploadedByUserId(),
                evidence.getTitle(), evidence.getDescription(), evidence.getSport(), evidence.getPosition(),
                evidence.getEventType(), evidence.getMatchOrTraining(), evidence.getEventDate(),
                evidence.getFileUrl(), evidence.getExternalVideoLink(), evidence.getMediaAssetId(),
                evidence.getVerificationStatus(), evidence.getAiAnalysisStatus(), evidence.getCreatedAt(),
                evidence.getUpdatedAt());
    }
}
