package za.co.ultronsport.web.dto;

public record VerificationSummaryResponse(
        long visibleEvidenceCount,
        long verifiedEvidenceCount,
        long pendingVerificationEvidenceCount,
        long rejectedEvidenceCount,
        long flaggedEvidenceCount,
        long archivedEvidenceCount
) {
}
