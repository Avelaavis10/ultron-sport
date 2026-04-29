package za.co.ultronsport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "evidence_uploads")
public class EvidenceUpload extends BaseEntity {

    @Column(nullable = false)
    private Long uploadedByUserId;

    @Column(nullable = false)
    private Long athleteProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvidenceType evidenceType;

    @Column(nullable = false)
    private String sport;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private LocalDate uploadDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvidenceContext evidenceContext;

    private String fileUrl;
    private String externalLink;

    @Column(length = 1200)
    private String metadataNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiAnalysisStatus aiAnalysisStatus;

    protected EvidenceUpload() {
    }

    private EvidenceUpload(Long uploadedByUserId, Long athleteProfileId, EvidenceType evidenceType, String sport,
                           String position, String eventType, LocalDate uploadDate, EvidenceContext evidenceContext,
                           String fileUrl, String externalLink, String metadataNotes) {
        this.uploadedByUserId = uploadedByUserId;
        this.athleteProfileId = athleteProfileId;
        this.evidenceType = evidenceType;
        this.sport = sport;
        this.position = position;
        this.eventType = eventType;
        this.uploadDate = uploadDate;
        this.evidenceContext = evidenceContext;
        this.fileUrl = fileUrl;
        this.externalLink = externalLink;
        this.metadataNotes = metadataNotes;
        this.verificationStatus = VerificationStatus.SUBMITTED;
        this.aiAnalysisStatus = AiAnalysisStatus.NOT_REQUESTED;
    }

    public static EvidenceUpload create(Long uploadedByUserId, Long athleteProfileId, EvidenceType evidenceType,
                                        String sport, String position, String eventType, LocalDate uploadDate,
                                        EvidenceContext evidenceContext, String fileUrl, String externalLink,
                                        String metadataNotes) {
        return new EvidenceUpload(uploadedByUserId, athleteProfileId, evidenceType, sport, position, eventType,
                uploadDate, evidenceContext, fileUrl, externalLink, metadataNotes);
    }

    public void markPendingVerification() {
        verificationStatus = VerificationStatus.PENDING_VERIFICATION;
    }

    public void markVerified() {
        verificationStatus = VerificationStatus.VERIFIED;
    }

    public void markRejected() {
        verificationStatus = VerificationStatus.REJECTED;
    }

    public void flag() {
        verificationStatus = VerificationStatus.FLAGGED;
    }

    public Long getUploadedByUserId() {
        return uploadedByUserId;
    }

    public Long getAthleteProfileId() {
        return athleteProfileId;
    }

    public EvidenceType getEvidenceType() {
        return evidenceType;
    }

    public String getSport() {
        return sport;
    }

    public String getPosition() {
        return position;
    }

    public String getEventType() {
        return eventType;
    }

    public LocalDate getUploadDate() {
        return uploadDate;
    }

    public EvidenceContext getEvidenceContext() {
        return evidenceContext;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public String getMetadataNotes() {
        return metadataNotes;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public AiAnalysisStatus getAiAnalysisStatus() {
        return aiAnalysisStatus;
    }
}
