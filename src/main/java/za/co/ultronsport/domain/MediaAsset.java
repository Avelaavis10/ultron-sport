package za.co.ultronsport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "media_assets", indexes = {
        @Index(name = "idx_media_owner_user", columnList = "owner_user_id"),
        @Index(name = "idx_media_athlete_profile", columnList = "athlete_profile_id"),
        @Index(name = "idx_media_evidence_upload", columnList = "evidence_upload_id"),
        @Index(name = "idx_media_created_at", columnList = "created_at")
})
public class MediaAsset extends BaseEntity {

    @Column(nullable = false)
    private Long ownerUserId;

    @Column(nullable = false)
    private Long athleteProfileId;

    private Long evidenceUploadId;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String storedFilename;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSizeBytes;

    @Column(nullable = false, length = 64)
    private String checksumSha256;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaStorageProvider storageProvider;

    @Column(nullable = false)
    private String storageKey;

    @Column(nullable = false)
    private String publicUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaUploadStatus uploadStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaScanStatus scanStatus;

    protected MediaAsset() {
    }

    private MediaAsset(Long ownerUserId, Long athleteProfileId, String originalFilename, String storedFilename,
                       String contentType, Long fileSizeBytes, String checksumSha256,
                       MediaStorageProvider storageProvider, String storageKey, String publicUrl) {
        this.ownerUserId = ownerUserId;
        this.athleteProfileId = athleteProfileId;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.checksumSha256 = checksumSha256;
        this.storageProvider = storageProvider;
        this.storageKey = storageKey;
        this.publicUrl = publicUrl;
        this.uploadStatus = MediaUploadStatus.UPLOADED;
        this.scanStatus = MediaScanStatus.SKIPPED_FOR_MVP;
    }

    public static MediaAsset uploaded(Long ownerUserId, Long athleteProfileId, String originalFilename,
                                      String storedFilename, String contentType, Long fileSizeBytes,
                                      String checksumSha256, MediaStorageProvider storageProvider,
                                      String storageKey, String publicUrl) {
        return new MediaAsset(ownerUserId, athleteProfileId, originalFilename, storedFilename, contentType,
                fileSizeBytes, checksumSha256, storageProvider, storageKey, publicUrl);
    }

    public void attachToEvidence(Long evidenceUploadId) {
        this.evidenceUploadId = evidenceUploadId;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public Long getAthleteProfileId() {
        return athleteProfileId;
    }

    public Long getEvidenceUploadId() {
        return evidenceUploadId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public MediaStorageProvider getStorageProvider() {
        return storageProvider;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public MediaUploadStatus getUploadStatus() {
        return uploadStatus;
    }

    public MediaScanStatus getScanStatus() {
        return scanStatus;
    }
}
