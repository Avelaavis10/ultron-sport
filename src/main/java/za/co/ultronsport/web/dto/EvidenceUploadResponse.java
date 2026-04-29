package za.co.ultronsport.web.dto;

import java.time.LocalDate;
import za.co.ultronsport.domain.AiAnalysisStatus;
import za.co.ultronsport.domain.EvidenceContext;
import za.co.ultronsport.domain.EvidenceType;
import za.co.ultronsport.domain.EvidenceUpload;
import za.co.ultronsport.domain.VerificationStatus;

public record EvidenceUploadResponse(
        Long id,
        Long uploadedByUserId,
        Long athleteProfileId,
        EvidenceType evidenceType,
        String sport,
        String position,
        String eventType,
        LocalDate uploadDate,
        EvidenceContext evidenceContext,
        String fileUrl,
        String externalLink,
        VerificationStatus verificationStatus,
        AiAnalysisStatus aiAnalysisStatus
) {
    public static EvidenceUploadResponse from(EvidenceUpload evidence) {
        return new EvidenceUploadResponse(evidence.getId(), evidence.getUploadedByUserId(),
                evidence.getAthleteProfileId(), evidence.getEvidenceType(), evidence.getSport(),
                evidence.getPosition(), evidence.getEventType(), evidence.getUploadDate(),
                evidence.getEvidenceContext(), evidence.getFileUrl(), evidence.getExternalLink(),
                evidence.getVerificationStatus(), evidence.getAiAnalysisStatus());
    }
}
