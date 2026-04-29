package za.co.ultronsport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "achievements")
public class Achievement extends BaseEntity {

    @Column(nullable = false)
    private Long athleteProfileId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private LocalDate achievedAt;
    private boolean verified;

    protected Achievement() {
    }

    private Achievement(Long athleteProfileId, String title, String description, LocalDate achievedAt) {
        this.athleteProfileId = athleteProfileId;
        this.title = title;
        this.description = description;
        this.achievedAt = achievedAt;
        this.verified = false;
    }

    public static Achievement create(Long athleteProfileId, String title, String description, LocalDate achievedAt) {
        return new Achievement(athleteProfileId, title, description, achievedAt);
    }

    public void markVerified() {
        verified = true;
    }

    public void updateDetails(String title, String description, LocalDate achievedAt) {
        if (verified) {
            throw new IllegalStateException("Verified achievements cannot be edited in the MVP workflow.");
        }
        this.title = title;
        this.description = description;
        this.achievedAt = achievedAt;
    }

    public Long getAthleteProfileId() {
        return athleteProfileId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getAchievedAt() {
        return achievedAt;
    }

    public boolean isVerified() {
        return verified;
    }
}
