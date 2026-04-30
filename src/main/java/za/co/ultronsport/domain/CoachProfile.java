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

    private Long organisationId;
    private String organisationName;
    private String sport;

    @Column(length = 1200)
    private String qualificationSummary;

    private Integer yearsExperience;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus;

    protected CoachProfile() {
    }

    private CoachProfile(Long userId, String certificationReference, Long organisationId, String organisationName,
                         String sport, String qualificationSummary, Integer yearsExperience) {
        this.userId = userId;
        this.certificationReference = certificationReference;
        this.organisationId = organisationId;
        this.organisationName = organisationName;
        this.sport = sport;
        this.qualificationSummary = qualificationSummary;
        this.yearsExperience = yearsExperience;
        this.verificationStatus = VerificationStatus.PENDING_VERIFICATION;
    }

    public static CoachProfile create(Long userId, String certificationReference, String organisationName, String sport) {
        return create(userId, certificationReference, null, organisationName, sport, null, null);
    }

    public static CoachProfile create(Long userId, String certificationReference, Long organisationId,
                                      String organisationName, String sport, String qualificationSummary,
                                      Integer yearsExperience) {
        return new CoachProfile(userId, certificationReference, organisationId, organisationName, sport,
                qualificationSummary, yearsExperience);
    }

    public void updateDetails(String certificationReference, Long organisationId, String organisationName,
                              String sport, String qualificationSummary, Integer yearsExperience) {
        this.certificationReference = certificationReference;
        this.organisationId = organisationId;
        this.organisationName = organisationName;
        this.sport = sport;
        this.qualificationSummary = qualificationSummary;
        this.yearsExperience = yearsExperience;
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

    public Long getOrganisationId() {
        return organisationId;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public String getSport() {
        return sport;
    }

    public String getQualificationSummary() {
        return qualificationSummary;
    }

    public Integer getYearsExperience() {
        return yearsExperience;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }
}
