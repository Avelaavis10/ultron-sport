package za.co.ultronsport.web.dto;

public record ModerationSummaryResponse(
        long flaggedEvidenceCount,
        long archivedEvidenceCount,
        long pendingVerificationCount,
        long verifiedEvidenceCount,
        long rejectedEvidenceCount
) {
}
