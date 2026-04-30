package za.co.ultronsport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "verification_requests")
public class VerificationRequest extends BaseEntity {

    @Column(nullable = false)
    private Long evidenceUploadId;

    private Long athleteProfileId;

    @Column(nullable = false)
    private Long requestedByUserId;

    @Column(nullable = false)
    private Long verifierUserId;

    private Long coachProfileId;
    private Long organisationId;
    private Boolean sharedOrganisationContext;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;

    @Column(length = 1000)
    private String comments;

    private Instant decidedAt;

    protected VerificationRequest() {
    }

    private VerificationRequest(Long evidenceUploadId, Long requestedByUserId, Long verifierUserId) {
        this.evidenceUploadId = evidenceUploadId;
        this.requestedByUserId = requestedByUserId;
        this.verifierUserId = verifierUserId;
        this.status = VerificationStatus.PENDING_VERIFICATION;
    }

    public static VerificationRequest create(Long evidenceUploadId, Long requestedByUserId, Long verifierUserId) {
        return new VerificationRequest(evidenceUploadId, requestedByUserId, verifierUserId);
    }

    public void attachContext(Long athleteProfileId, Long coachProfileId, Long organisationId,
                              Boolean sharedOrganisationContext) {
        this.athleteProfileId = athleteProfileId;
        this.coachProfileId = coachProfileId;
        this.organisationId = organisationId;
        this.sharedOrganisationContext = sharedOrganisationContext;
    }

    public void approve(String comments) {
        complete(VerificationStatus.VERIFIED, comments);
    }

    public void reject(String comments) {
        complete(VerificationStatus.REJECTED, comments);
    }

    public void flag(String comments) {
        complete(VerificationStatus.FLAGGED, comments);
    }

    private void complete(VerificationStatus nextStatus, String comments) {
        if (status != VerificationStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Verification request has already been decided.");
        }
        this.status = nextStatus;
        this.comments = comments;
        this.decidedAt = Instant.now();
    }

    public Long getEvidenceUploadId() {
        return evidenceUploadId;
    }

    public Long getAthleteProfileId() {
        return athleteProfileId;
    }

    public Long getRequestedByUserId() {
        return requestedByUserId;
    }

    public Long getVerifierUserId() {
        return verifierUserId;
    }

    public Long getCoachProfileId() {
        return coachProfileId;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public Boolean getSharedOrganisationContext() {
        return sharedOrganisationContext;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public String getComments() {
        return comments;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
