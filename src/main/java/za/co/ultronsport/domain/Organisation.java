package za.co.ultronsport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "organisations")
public class Organisation extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String location;

    private String contactEmail;
    private Long primaryAdminUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus;

    protected Organisation() {
    }

    private Organisation(String name, String type, String location, String contactEmail, Long primaryAdminUserId) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.contactEmail = contactEmail;
        this.primaryAdminUserId = primaryAdminUserId;
        this.verificationStatus = VerificationStatus.PENDING_VERIFICATION;
    }

    public static Organisation create(String name, String type, String location, Long primaryAdminUserId) {
        return create(name, type, location, null, primaryAdminUserId);
    }

    public static Organisation create(String name, String type, String location, String contactEmail,
                                      Long primaryAdminUserId) {
        return new Organisation(name, type, location, contactEmail, primaryAdminUserId);
    }

    public void updateDetails(String name, String type, String location, String contactEmail,
                              VerificationStatus verificationStatus) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.contactEmail = contactEmail;
        this.verificationStatus = verificationStatus;
    }

    public void markVerified() {
        verificationStatus = VerificationStatus.VERIFIED;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public Long getPrimaryAdminUserId() {
        return primaryAdminUserId;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }
}
