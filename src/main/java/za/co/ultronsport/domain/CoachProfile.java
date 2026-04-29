package za.co.ultronsport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "coach_profiles")
public class CoachProfile extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String certificationReference;

    private String organisationName;
    private String sport;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus;

    protected CoachProfile() {
    }

    private CoachProfile(Long userId, String certificationReference, String organisationName, String sport) {
        this.userId = userId;
        this.certificationReference = certificationReference;
        this.organisationName = organisationName;
        this.sport = sport;
        this.verificationStatus = VerificationStatus.PENDING_VERIFICATION;
    }

    public static CoachProfile create(Long userId, String certificationReference, String organisationName, String sport) {
        return new CoachProfile(userId, certificationReference, organisationName, sport);
    }

    public void approveVerifierStatus() {
        verificationStatus = VerificationStatus.VERIFIED;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCertificationReference() {
        return certificationReference;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public String getSport() {
        return sport;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }
}
