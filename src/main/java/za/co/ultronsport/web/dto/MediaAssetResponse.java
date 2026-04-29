package za.co.ultronsport.web.dto;

import java.time.Instant;
import za.co.ultronsport.domain.MediaAsset;
import za.co.ultronsport.domain.MediaScanStatus;
import za.co.ultronsport.domain.MediaStorageProvider;
import za.co.ultronsport.domain.MediaUploadStatus;

public record MediaAssetResponse(
        Long id,
        Long ownerUserId,
        Long athleteProfileId,
        Long evidenceUploadId,
        String originalFilename,
        String contentType,
        Long fileSizeBytes,
        String checksumSha256,
        MediaStorageProvider storageProvider,
        String publicUrl,
        MediaUploadStatus uploadStatus,
        MediaScanStatus scanStatus,
        Instant createdAt,
        Instant updatedAt
) {
    public static MediaAssetResponse from(MediaAsset asset) {
        return new MediaAssetResponse(asset.getId(), asset.getOwnerUserId(), asset.getAthleteProfileId(),
                asset.getEvidenceUploadId(), asset.getOriginalFilename(), asset.getContentType(),
                asset.getFileSizeBytes(), asset.getChecksumSha256(), asset.getStorageProvider(),
                asset.getPublicUrl(), asset.getUploadStatus(), asset.getScanStatus(), asset.getCreatedAt(),
                asset.getUpdatedAt());
    }
}
