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

    private String location;
    private Long primaryAdminUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus;

    protected Organisation() {
    }

    private Organisation(String name, String type, String location, Long primaryAdminUserId) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.primaryAdminUserId = primaryAdminUserId;
        this.verificationStatus = VerificationStatus.PENDING_VERIFICATION;
    }

    public static Organisation create(String name, String type, String location, Long primaryAdminUserId) {
        return new Organisation(name, type, location, primaryAdminUserId);
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

    public Long getPrimaryAdminUserId() {
        return primaryAdminUserId;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }
}
