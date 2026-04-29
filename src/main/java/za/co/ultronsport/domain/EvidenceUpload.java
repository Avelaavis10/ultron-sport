package za.co.ultronsport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "evidence_uploads", indexes = {
        @Index(name = "idx_evidence_athlete_profile", columnList = "athlete_profile_id"),
        @Index(name = "idx_evidence_status", columnList = "verification_status"),
        @Index(name = "idx_evidence_sport", columnList = "sport"),
        @Index(name = "idx_evidence_position", columnList = "position"),
        @Index(name = "idx_evidence_event_date", columnList = "event_date"),
        @Index(name = "idx_evidence_created_at", columnList = "created_at"),
        @Index(name = "idx_evidence_updated_at", columnList = "updated_at")
})
public class EvidenceUpload extends BaseEntity {

    @Column(nullable = false)
    private Long athleteProfileId;

    @Column(nullable = false)
    private Long uploadedByUserId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1200)
    private String description;

    @Column(nullable = false)
    private String sport;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvidenceContext matchOrTraining;

    @Column(nullable = false)
    private LocalDate eventDate;

    private String fileUrl;
    private String externalVideoLink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiAnalysisStatus aiAnalysisStatus;

    protected EvidenceUpload() {
    }

    private EvidenceUpload(Long uploadedByUserId, Long athleteProfileId, String title, String description,
                           String sport, String position, String eventType, EvidenceContext matchOrTraining,
                           LocalDate eventDate, String fileUrl, String externalVideoLink) {
        this.uploadedByUserId = uploadedByUserId;
        this.athleteProfileId = athleteProfileId;
        this.title = title;
        this.description = description;
        this.sport = sport;
        this.position = position;
        this.eventType = eventType;
        this.matchOrTraining = matchOrTraining;
        this.eventDate = eventDate;
        this.fileUrl = fileUrl;
        this.externalVideoLink = externalVideoLink;
        this.verificationStatus = VerificationStatus.DRAFT;
        this.aiAnalysisStatus = AiAnalysisStatus.NOT_STARTED;
    }

    public static EvidenceUpload createDraft(Long uploadedByUserId, Long athleteProfileId, String title,
                                             String description, String sport, String position, String eventType,
                                             EvidenceContext matchOrTraining, LocalDate eventDate, String fileUrl,
                                             String externalVideoLink) {
        return new EvidenceUpload(uploadedByUserId, athleteProfileId, title, description, sport, position, eventType,
                matchOrTraining, eventDate, fileUrl, externalVideoLink);
    }

    public void updateDetails(String title, String description, String sport, String position, String eventType,
                              EvidenceContext matchOrTraining, LocalDate eventDate, String fileUrl,
                              String externalVideoLink) {
        if (!isEditableByAthlete()) {
            throw new IllegalStateException("Evidence can only be edited while DRAFT or REJECTED.");
        }
        this.title = title;
        this.description = description;
        this.sport = sport;
        this.position = position;
        this.eventType = eventType;
        this.matchOrTraining = matchOrTraining;
        this.eventDate = eventDate;
        this.fileUrl = fileUrl;
        this.externalVideoLink = externalVideoLink;
    }

    public void submit() {
        if (!isEditableByAthlete()) {
            throw new IllegalStateException("Only DRAFT or REJECTED evidence can be submitted.");
        }
        verificationStatus = VerificationStatus.PENDING_VERIFICATION;
    }

    public void markPendingVerification() {
        submit();
    }

    public void verify() {
        if (verificationStatus != VerificationStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Only PENDING_VERIFICATION evidence can be verified.");
        }
        verificationStatus = VerificationStatus.VERIFIED;
    }

    public void markVerified() {
        verify();
    }

    public void reject() {
        if (verificationStatus != VerificationStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Only PENDING_VERIFICATION evidence can be rejected.");
        }
        verificationStatus = VerificationStatus.REJECTED;
    }

    public void markRejected() {
        reject();
    }

    public void flag() {
        if (verificationStatus == VerificationStatus.ARCHIVED) {
            throw new IllegalStateException("Archived evidence cannot be flagged.");
        }
        verificationStatus = VerificationStatus.FLAGGED;
    }

    public void archive() {
        if (verificationStatus == VerificationStatus.ARCHIVED) {
            throw new IllegalStateException("Evidence is already archived.");
        }
        verificationStatus = VerificationStatus.ARCHIVED;
    }

    public boolean isEditableByAthlete() {
        return verificationStatus == VerificationStatus.DRAFT || verificationStatus == VerificationStatus.REJECTED;
    }

    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }

    public boolean isPendingVerification() {
        return verificationStatus == VerificationStatus.PENDING_VERIFICATION;
    }

    public Long getAthleteProfileId() {
        return athleteProfileId;
    }

    public Long getUploadedByUserId() {
        return uploadedByUserId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
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

    public EvidenceContext getMatchOrTraining() {
        return matchOrTraining;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getExternalVideoLink() {
        return externalVideoLink;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public AiAnalysisStatus getAiAnalysisStatus() {
        return aiAnalysisStatus;
    }
}
